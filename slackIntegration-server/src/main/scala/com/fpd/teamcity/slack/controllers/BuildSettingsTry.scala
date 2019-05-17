package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.SlackGateway.{Destination, SlackChannel, SlackUser, attachmentToSlackMessage}
import com.fpd.teamcity.slack._
import jetbrains.buildServer.serverSide.{BuildHistory, SFinishedBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import jetbrains.buildServer.web.util.SessionUser
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Try}

class BuildSettingsTry(buildHistory: BuildHistory,
                       configManager: ConfigManager,
                       gateway: SlackGateway,
                       controllerManager: WebControllerManager,
                       val permissionManager: PermissionManager,
                       messageBuilderFactory: MessageBuilderFactory,
                       implicit val descriptor: PluginDescriptor
                      )
  extends SlackController {

  import BuildSettingsTry._
  import Strings.BuildSettingsTry._

  controllerManager.registerController(Resources.buildSettingTry.controllerUrl, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = Try {
    val id = request.param("id")
      .getOrElse(throw HandlerException(emptyIdParam))
    val setting = configManager.buildSetting(id)
      .getOrElse(throw HandlerException(buildSettingNotFound))
    val build = findPreviousBuild(buildHistory, setting)
      .getOrElse(throw HandlerException(previousBuildNotFound))

    detectDestination(setting, SessionUser.getUser(request)) match {
      case Some(dest) ⇒
        val future = gateway.sendMessage(dest,
          attachmentToSlackMessage(
            messageBuilderFactory.createForBuild(build).compile(setting.messageTemplate, Some(setting)),
            configManager.sendAsAttachment.exists(x ⇒ x)
          ))
        Await.result(future, 10 seconds) match {
          case Failure(error) ⇒ throw HandlerException(error.getMessage)
          case _ ⇒ messageSent(dest.toString)
        }
      case _ ⇒
        throw HandlerException(unknownDestination)
    }
  } recover { case x: HandlerException ⇒ s"Error: ${x.getMessage}" } map {
    ajaxView
  } get

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("id").exists(id ⇒ permissionManager.settingAccessPermitted(request, id))
}

object BuildSettingsTry {
  def findPreviousBuild(buildHistory: BuildHistory, setting: BuildSetting): Option[SFinishedBuild] =
    buildHistory.getEntries(false).asScala.find(b ⇒ !b.isPersonal && b.matchBranch(setting.branchMask) && b.getBuildTypeId == setting.buildTypeId)

  def detectDestination(setting: BuildSetting, user: ⇒ SUser): Option[Destination] = setting.slackChannel.isEmpty match {
    case true if setting.notifyCommitter ⇒
      Some(SlackUser(user.getEmail))
    case false ⇒
      Some(SlackChannel(setting.slackChannel))
    case _ ⇒
      None
  }

  case class HandlerException(message: String) extends Exception(message)
}
