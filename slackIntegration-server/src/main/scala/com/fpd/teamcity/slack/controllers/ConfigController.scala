package com.fpd.teamcity.slack.controllers

import java.net.URLEncoder
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
  import Helpers.Implicits._
  import ConfigController._

  controllerManager.registerController(Resources.configPage.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      oauthKey ← request.param("oauthKey")
      publicUrl ← request.param("publicUrl")
    } yield {
      val newConfig = ConfigManager.Config(oauthKey)

      slackGateway.sessionByConfig(newConfig).map { _ ⇒
        configManager.update(oauthKey, publicUrl)
      } match {
        case Some(x) if x ⇒ Left(x)
        case Some(_) ⇒ Right("Failed to update OAuth Access Token")
        case None ⇒ Right("Unable to create session by config")
      }
    }

    val either = result.getOrElse(Right("Param oauthKey is missing"))

    redirectTo(createRedirect(either), response)
  }
}

object ConfigController {
  private def createRedirect(either: Either[Boolean, String]): String = either match {
    case Left(_) ⇒ s"/admin/admin.html?item=${Strings.tabId}"
    case Right(error) ⇒ s"/admin/admin.html?item=${Strings.tabId}&error=${URLEncoder.encode(error, "UTF-8")}"
  }
}
