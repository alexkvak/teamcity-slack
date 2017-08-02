package com.fpd.teamcity.slack.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.controllers.SlackController
import com.fpd.teamcity.slack.{ConfigManager, MessageBuilder, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingEditPage(controllerManager: WebControllerManager,
                           descriptor: PluginDescriptor,
                           config: ConfigManager
                          ) extends BaseController with SlackController {
  controllerManager.registerController(Resources.buildSettingEdit.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    import com.fpd.teamcity.slack.Helpers.Implicits._

    val view = descriptor.getPluginResourcesPath(Resources.buildSettingEdit.view)

    val result = for {
      key ← request.param("id")
      model ← config.buildSetting(key)
    } yield {
      new ModelAndView(view, Map("model" → model, "key" → key, "defaultMessage" → MessageBuilder.defaultMessage).asJava)
    }

    result getOrElse new ModelAndView(view)
  }
}
