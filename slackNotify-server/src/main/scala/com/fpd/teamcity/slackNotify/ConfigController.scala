package com.fpd.teamcity.slackNotify

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

object ConfigController {
  def emptyAsNone(s: String): Option[String] = Option(s).filterNot(_.trim.isEmpty)
}

class ConfigController(config: ConfigManager, webControllerManager: WebControllerManager) extends BaseController {
  webControllerManager.registerController("/app/slackNotify/**", this)

  override def doHandle(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): ModelAndView = {
    def param(name: String) = ConfigController.emptyAsNone(httpServletRequest.getParameter(name))

    config.updateAndPersist(Config(
      param("oauthKey")
    ))

    new ModelAndView(new RedirectView("/admin/admin.html?item=SlackNotifier"))
  }
}
