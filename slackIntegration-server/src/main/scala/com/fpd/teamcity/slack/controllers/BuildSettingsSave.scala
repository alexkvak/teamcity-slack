package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag}
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.Strings.BuildSettingsController._
import com.fpd.teamcity.slack.{
  ConfigManager,
  PermissionManager,
  Resources,
  SlackGateway
}
import jetbrains.buildServer.web.openapi.{
  PluginDescriptor,
  WebControllerManager
}
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.Try

class BuildSettingsSave(
    val configManager: ConfigManager,
    controllerManager: WebControllerManager,
    slackGateway: SlackGateway,
    val permissionManager: PermissionManager,
    implicit val descriptor: PluginDescriptor
) extends SlackController {

  controllerManager.registerController(
    Resources.buildSettingSave.controllerUrl,
    this
  )

  override def handle(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ModelAndView =
    ajaxView(handleSave(request))

  def handleSave(request: HttpServletRequest): String = {
    def flags = {
      val keyToFlag = Map(
        "success" -> BuildSettingFlag.success,
        "failureToSuccess" -> BuildSettingFlag.failureToSuccess,
        "fail" -> BuildSettingFlag.failure,
        "successToFailure" -> BuildSettingFlag.successToFailure,
        "started" -> BuildSettingFlag.started,
        "canceled" -> BuildSettingFlag.canceled,
        "queued" -> BuildSettingFlag.queued
      )
      val keys = keyToFlag.keys.filter(key => request.param(key).isDefined)
      keys.map(keyToFlag).toSet
    }

    val result = for {
      // preparing params
      branch <- request.param("branchMask")
      buildId <- request.param("buildTypeId")
      message <- request.param("messageTemplate")
    } yield {
      lazy val artifactsMask = request.param("artifactsMask")
      val channel = request.param("slackChannel").getOrElse("")
      val notifyCommitter = request.param("notifyCommitter").isDefined
      val maxVcsChanges = request
        .param("maxVcsChanges")
        .getOrElse(BuildSetting.defaultMaxVCSChanges.toString)
        .toInt

      // store build setting
      def updateConfig() = configManager.updateBuildSetting(
        BuildSetting(
          buildId,
          branch,
          channel,
          message,
          flags,
          artifactsMask.getOrElse(""),
          request.param("deepLookup").isDefined,
          notifyCommitter,
          maxVcsChanges
        ),
        request.param("key")
      )

      // check channel availability
      if (channel.isEmpty && !notifyCommitter) {
        channelOrNotifyCommitterError
      } else if (Try(branch.r).isFailure) {
        compileBranchMaskError
      } else if (artifactsMask.exists(s => Try(s.r).isFailure)) {
        compileArtifactsMaskError
      } else {
        if (
          channel.nonEmpty && !slackGateway
            .isChannelExists(channel)
            .getOrElse(false)
        ) {
          channelNotFoundError(channel)
        } else {
          updateConfig() match {
            case Some(_) => ""
            case _       => emptyConfigError
          }
        }
      }
    }

    result getOrElse requirementsError
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request
      .param("buildTypeId")
      .exists(permissionManager.buildAccessPermitted(request, _))
}
