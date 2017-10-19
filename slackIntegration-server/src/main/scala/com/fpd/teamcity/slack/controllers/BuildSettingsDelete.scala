package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.{ConfigManager, PermissionManager, Resources}
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

class BuildSettingsDelete(configManager: ConfigManager,
                          controllerManager: WebControllerManager,
                          val permissionManager: PermissionManager,
                          implicit val descriptor: PluginDescriptor
                         )
  extends SlackController {

  controllerManager.registerController(Resources.buildSettingDelete.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      id ← request.param("id")
      result ← configManager.removeBuildSetting(id)
    } yield result

    ajaxView(result.filter(_ == true).map(_ ⇒ "") getOrElse "Something went wrong")
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("id").exists(id ⇒ permissionManager.settingAccessPermitted(request, id))
}
