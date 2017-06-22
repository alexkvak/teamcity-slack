package com.fpd.teamcity.slackNotify

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

object ConfigController {
  def emptyAsNone(s: String): Option[String] = Option(s).filterNot(_.trim.isEmpty)
}

class ConfigController(configManager: ConfigManager, controllerManager: WebControllerManager) extends BaseController {
  import ConfigController._

  controllerManager.registerController(Resources.configPage.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def param(name: String) = emptyAsNone(request.getParameter(name))

    val newConfig = for {
      oauthKey ← param("oauthKey")
    } yield {
      ConfigManager.Config(oauthKey)
    }

    val option = newConfig map { config ⇒
      SlackGateway.sessionByConfig(config).map { _ ⇒
        configManager.updateAndPersist(config)
      }
    }

    new ModelAndView(new RedirectView(createRedirect(option)))
  }

  private def createRedirect[T](result: Option[T]): String = result.map(_ ⇒
    s"/admin/admin.html?item=${Strings.tabId}"
  ).getOrElse(
    s"/admin/admin.html?item=${Strings.tabId}&error=1"
  )
}
