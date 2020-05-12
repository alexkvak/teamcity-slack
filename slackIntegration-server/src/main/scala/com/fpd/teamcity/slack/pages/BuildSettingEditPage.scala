package com.fpd.teamcity.slack.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.controllers.SlackController
import com.fpd.teamcity.slack.{ConfigManager, SBuildMessageBuilder, PermissionManager, Resources}
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingEditPage(controllerManager: WebControllerManager,
                           descriptor: PluginDescriptor,
                           val permissionManager: PermissionManager,
                           config: ConfigManager
                          ) extends SlackController {
  controllerManager.registerController(Resources.buildSettingEdit.controllerUrl, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    import com.fpd.teamcity.slack.Helpers.Implicits._

    val view = descriptor.getPluginResourcesPath(Resources.buildSettingEdit.view)

    val result = for {
      key ← request.param("id")
      model ← config.buildSetting(key)
    } yield {
      new ModelAndView(view, Map("model" → model, "key" → key).asJava)
    }

    result getOrElse new ModelAndView(view, Map("defaultMessage" → SBuildMessageBuilder.defaultMessage).asJava)
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("buildTypeId").exists(id ⇒ permissionManager.buildAccessPermitted(request, id))
}
