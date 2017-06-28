package com.fpd.teamcity.slackNotify.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify._
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

class ConfigController(configManager: ConfigManager, controllerManager: WebControllerManager) extends BaseController {
  import Helpers._

  controllerManager.registerController(Resources.configPage.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val newConfig = for {
      oauthKey ← request.param("oauthKey")
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
