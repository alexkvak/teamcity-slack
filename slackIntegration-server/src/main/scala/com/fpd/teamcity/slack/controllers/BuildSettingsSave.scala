package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag}
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.{ConfigManager, Resources, SlackGateway}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.util.Try

class BuildSettingsSave(configManager: ConfigManager,
                        controllerManager: WebControllerManager,
                        slackGateway: SlackGateway,
                        implicit val descriptor: PluginDescriptor
                       )
  extends BaseController with SlackController {

  controllerManager.registerController(Resources.buildSettingSave.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def flags = {
      val keyToFlag = Map(
        "success" → BuildSettingFlag.success,
        "failureToSuccess" → BuildSettingFlag.failureToSuccess,
        "fail" → BuildSettingFlag.failure,
        "successToFailure" → BuildSettingFlag.successToFailure,
        "canceled" → BuildSettingFlag.canceled
      )
      val keys = keyToFlag.keys.filter(key ⇒ request.param(key).isDefined)
      keys.map(keyToFlag).toSet
    }

    val artifactsMask = request.param("artifactsMask")

    val result = for {
    // preparing params
      branch ← request.param("branchMask")
      channel ← request.param("slackChannel")
      buildId ← request.param("buildTypeId")
      message ← request.param("messageTemplate")

      config ← configManager.config
    } yield {
      // store build setting
      def updateConfig() = configManager.updateBuildSetting(
        BuildSetting(buildId, branch, channel, message, flags, artifactsMask.getOrElse(""), request.param("deepLookup").isDefined),
        request.param("key")
      ).map(_ ⇒ "").getOrElse("")

      // check channel availability
      slackGateway.sessionByConfig(config) match {
        case Some(session) ⇒
          Option(session.findChannelByName(channel)) match {
            case Some(_) if Try(branch.r).isFailure ⇒ s"Unable to compile regular expression $branch"
            case Some(_) if artifactsMask.isDefined && Try(artifactsMask.get.r).isFailure ⇒ s"Unable to compile regular expression ${artifactsMask.get}"
            case Some(_) ⇒ updateConfig()
            case None ⇒ s"Unable to find channel with name $channel"
          }
        case _ ⇒
          "Unable to create session by config"
      }
    }

    ajaxView(result getOrElse "Unknown error")
  }
}
