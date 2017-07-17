package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag}
import com.fpd.teamcity.slack.Helpers._
import com.fpd.teamcity.slack.{ConfigManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.util.Try

class BuildSettingsSave(configManager: ConfigManager,
                        controllerManager: WebControllerManager,
                        implicit val descriptor: PluginDescriptor
                       )
  extends BaseController with SlackController {

  controllerManager.registerController(Resources.buildSettingSave.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def collectFlags = {
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
      branchMask ← request.param("branchMask")
      slackChannel ← request.param("slackChannel")
      buildTypeId ← request.param("buildTypeId")
      messageTemplate ← request.param("messageTemplate")
      result ← configManager.updateBuildSetting(BuildSetting(buildTypeId, branchMask, slackChannel, messageTemplate, collectFlags), request.param("key")) if Try(branchMask.r).isSuccess
    } yield result

    ajaxView(result.filter(_ == true).map(_ ⇒ "") getOrElse "Something went wrong")
  }
}
