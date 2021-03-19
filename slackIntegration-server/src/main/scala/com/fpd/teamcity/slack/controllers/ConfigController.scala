package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.Strings.ConfigController._
import com.fpd.teamcity.slack._
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView

import java.net.URLEncoder
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.Try

class ConfigController(
    configManager: ConfigManager,
    controllerManager: WebControllerManager,
    val permissionManager: PermissionManager,
    slackGateway: SlackGateway
) extends SlackController {
  import ConfigController._
  import Helpers.Implicits._

  controllerManager.registerController(Resources.configPage.controllerUrl, this)

  override def handle(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ModelAndView = {
    val result = for {
      oauthKey <- request.param("oauthKey")
    } yield {
      val newConfig = ConfigManager.Config(oauthKey)
      val publicUrl = request.param("publicUrl").getOrElse("")
      val senderName = request.param("senderName").getOrElse("")

      slackGateway.sessionByConfig(newConfig) match {
        case Some(_) =>
          configManager.update(
            oauthKey,
            publicUrl,
            request.param("personalEnabled").isDefined,
            request.param("enabled").isDefined,
            senderName,
            request.param("sendAsAttachment").isDefined
          )
          Left(true)
        case _ => Right(sessionByConfigError("auth error"))
      }
    }

    val either = result.getOrElse(Right(oauthKeyParamMissing))

    redirectTo(createRedirect(either, request.getContextPath), response)
  }
}

object ConfigController {
  private def createRedirect(
      either: Either[Boolean, String],
      context: String
  ): String = either match {
    case Left(_) => s"$context/admin/admin.html?item=${Strings.tabId}"
    case Right(error) =>
      s"$context/admin/admin.html?item=${Strings.tabId}&error=${URLEncoder
        .encode(error, "UTF-8")}"
  }
}
