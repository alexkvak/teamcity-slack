package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag}
import com.fpd.teamcity.slack.Helpers._
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
        "successToFailure" → BuildSettingFlag.successToFailure
      )
      val keys = keyToFlag.keys.filter(key ⇒ request.param(key).isDefined)

      keys.foldLeft(Set.empty[BuildSettingFlag])((acc, flag) ⇒ acc + keyToFlag(flag))
    }

    val result = for {
      // preparing params
      branch ← request.param("branchMask")
      channel ← request.param("slackChannel")
      buildId ← request.param("buildTypeId")
      message ← request.param("messageTemplate")

      // check channel availability
      config ← configManager.config
      session ← slackGateway.sessionByConfig(config)
      _ ← Option(session.findChannelById(channel))

      // store build setting
      result ← configManager.updateBuildSetting(BuildSetting(buildId, branch, channel, message, flags), request.param("key")) if Try(branch.r).isSuccess
    } yield result

    ajaxView(result.filter(_ == true).map(_ ⇒ "") getOrElse "Something went wrong")
  }
}
