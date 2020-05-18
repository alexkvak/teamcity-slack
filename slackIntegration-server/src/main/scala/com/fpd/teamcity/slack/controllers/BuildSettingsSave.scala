package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag}
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.Strings.BuildSettingsController._
import com.fpd.teamcity.slack.{ConfigManager, PermissionManager, Resources, SlackGateway}
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.util.{Failure, Success, Try}

class BuildSettingsSave(val configManager: ConfigManager,
                        controllerManager: WebControllerManager,
                        slackGateway: SlackGateway,
                        val permissionManager: PermissionManager,
                        implicit val descriptor: PluginDescriptor
                       )
  extends SlackController {

  controllerManager.registerController(Resources.buildSettingSave.controllerUrl, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView =
    ajaxView(handleSave(request))

  def handleSave(request: HttpServletRequest): String = {
    def flags = {
      val keyToFlag = Map(
        "success" → BuildSettingFlag.success,
        "failureToSuccess" → BuildSettingFlag.failureToSuccess,
        "fail" → BuildSettingFlag.failure,
        "successToFailure" → BuildSettingFlag.successToFailure,
        "started" → BuildSettingFlag.started,
        "canceled" → BuildSettingFlag.canceled,
        "queued" → BuildSettingFlag.queued
      )
      val keys = keyToFlag.keys.filter(key ⇒ request.param(key).isDefined)
      keys.map(keyToFlag).toSet
    }

    val result = for {
    // preparing params
      branch ← request.param("branchMask")
      buildId ← request.param("buildTypeId")
      message ← request.param("messageTemplate")

      config ← configManager.config
    } yield {
      lazy val artifactsMask = request.param("artifactsMask")
      val channel = request.param("slackChannel")
      val notifyCommitter = request.param("notifyCommitter").isDefined
      val maxVcsChanges = request.param("maxVcsChanges").getOrElse(BuildSetting.defaultMaxVCSChanges.toString).toInt

      // store build setting
      def updateConfig() = configManager.updateBuildSetting(
        BuildSetting(buildId, branch, channel.getOrElse(""), message, flags, artifactsMask.getOrElse(""), request.param("deepLookup").isDefined, notifyCommitter, maxVcsChanges),
        request.param("key")
      )

      // check channel availability
      if (!channel.exists(_.nonEmpty) && !notifyCommitter) {
        channelOrNotifyCommitterError
      } else if (Try(branch.r).isFailure) {
        compileBranchMaskError
      } else if (artifactsMask.exists(s ⇒ Try(s.r).isFailure)) {
        compileArtifactsMaskError
      } else {
        slackGateway.sessionByConfig(config) match {
          case Success(session) ⇒
            if (channel.exists(s ⇒ null == session.findChannelByName(s))) {
              channelNotFoundError(channel.get)
            } else {
              updateConfig() match {
                case Some(_) ⇒ ""
                case _ ⇒ emptyConfigError
              }
            }
          case Failure(e) ⇒
            sessionByConfigError(e.getMessage)
        }
      }
    }

    result getOrElse requirementsError
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("buildTypeId").exists(permissionManager.buildAccessPermitted(request, _))
}
