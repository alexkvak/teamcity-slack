package com.fpd.teamcity.slackNotify.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify.Helpers._
import com.fpd.teamcity.slackNotify.{ConfigManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

class BuildSettingsDelete(configManager: ConfigManager,
                          controllerManager: WebControllerManager,
                          implicit val descriptor: PluginDescriptor
                         )
  extends BaseController with SlackController {

  controllerManager.registerController(Resources.buildSettingDelete.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      id ← request.param("id")
      result ← configManager.removeBuildSetting(id)
    } yield result

    ajaxView(result.filter(_ == true).map(_ ⇒ "") getOrElse "Something went wrong")
  }
}
