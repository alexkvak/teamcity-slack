package com.fpd.teamcity.slackNotify.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify.{ConfigManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingEditPage(controllerManager: WebControllerManager,
                           descriptor: PluginDescriptor,
                           config: ConfigManager
                          ) extends BaseController {
  controllerManager.registerController(Resources.buildSettingEdit.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    import com.fpd.teamcity.slackNotify.Helpers._

    val view = descriptor.getPluginResourcesPath(Resources.buildSettingEdit.view)

    // TODO: optimize
    (for {
      key ← request.param("id")
      model ← config.buildSetting(key)
    } yield {
      new ModelAndView(view, Map("model" → model, "key" → key).asJava)
    }) getOrElse {
      new ModelAndView(view)
    }
  }
}
