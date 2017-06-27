package com.fpd.teamcity.slackNotify.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify.{ConfigManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingListPage(controllerManager: WebControllerManager,
                           descriptor: PluginDescriptor,
                           config: ConfigManager
                          ) extends BaseController {
  controllerManager.registerController(Resources.buildSettingListPage.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val view = descriptor.getPluginResourcesPath(Resources.buildSettingListPage.view)
    new ModelAndView(view, Map("list" → config.buildSettingList.asJava).asJava)
  }
}
