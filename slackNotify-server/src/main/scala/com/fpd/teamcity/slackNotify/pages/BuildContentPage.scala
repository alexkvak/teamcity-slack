package com.fpd.teamcity.slackNotify.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify.Resources
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

class BuildContentPage(controllerManager: WebControllerManager, descriptor: PluginDescriptor) extends BaseController {
  controllerManager.registerController(Resources.buildContentPage.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    new ModelAndView(descriptor.getPluginResourcesPath(Resources.buildContentPage.view))
  }
}
