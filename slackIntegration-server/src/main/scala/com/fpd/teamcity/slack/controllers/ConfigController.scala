package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack._
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView

class ConfigController(
                        configManager: ConfigManager,
                        controllerManager: WebControllerManager,
                        slackGateway: SlackGateway
                      )
  extends BaseController with SlackController {
  import Helpers._

  controllerManager.registerController(Resources.configPage.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val newConfig = for {
      oauthKey ← request.param("oauthKey")
    } yield {
      ConfigManager.Config(oauthKey)
    }

    val option = newConfig map { config ⇒
      slackGateway.sessionByConfig(config).map { _ ⇒
        configManager.updateAndPersist(config)
      }
    }

    redirectTo(createRedirect(option), response)
  }

  private def createRedirect[T](result: Option[T]): String = result.map(_ ⇒
    s"/admin/admin.html?item=${Strings.tabId}"
  ).getOrElse(
    s"/admin/admin.html?item=${Strings.tabId}&error=1"
  )
}
