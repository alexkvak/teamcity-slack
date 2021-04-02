package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.SlackGateway.{
  Destination,
  SlackChannel,
  SlackUser,
  attachmentToSlackMessage
}
import com.fpd.teamcity.slack._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import jetbrains.buildServer.serverSide.{ProjectManager, SFinishedBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.{
  PluginDescriptor,
  WebControllerManager
}
import jetbrains.buildServer.web.util.SessionUser
import org.springframework.web.servlet.ModelAndView

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Try}

class BuildSettingsTry(
    projectManager: ProjectManager,
    configManager: ConfigManager,
    gateway: SlackGateway,
    controllerManager: WebControllerManager,
    val permissionManager: PermissionManager,
    messageBuilderFactory: MessageBuilderFactory,
    implicit val descriptor: PluginDescriptor
) extends SlackController {

  import BuildSettingsTry._
  import Strings.BuildSettingsTry._

  controllerManager.registerController(
    Resources.buildSettingTry.controllerUrl,
    this
  )

  override def handle(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ModelAndView = Try {
    val id = request
      .param("id")
      .getOrElse(throw HandlerException(emptyIdParam))
    val setting = configManager
      .buildSetting(id)
      .getOrElse(throw HandlerException(buildSettingNotFound))
    val build = findPreviousBuild(projectManager, setting)
      .getOrElse(throw HandlerException(previousBuildNotFound))

    detectDestination(setting, SessionUser.getUser(request)) match {
      case Some(dest) =>
        val attachment = messageBuilderFactory
          .createForBuild(build)
          .compile(setting.messageTemplate, setting)

        val future = gateway.sendMessage(
          dest,
          attachmentToSlackMessage(
            attachment,
            configManager.sendAsAttachment.exists(x => x)
          )
        )

        Try(Await.result(future, 30 seconds)) match {
          case Failure(error) => throw HandlerException(error.getMessage)
          case _              => messageSent(dest.toString)
        }
      case _ =>
        throw HandlerException(unknownDestination)
    }
  } recover { case x: HandlerException => s"Error: ${x.getMessage}" } map {
    ajaxView
  } get

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request
      .param("id")
      .exists(id => permissionManager.settingAccessPermitted(request, id))
}

object BuildSettingsTry {
  @tailrec
  def filterMatchBuild(
      setting: BuildSetting,
      build: SFinishedBuild
  ): Option[SFinishedBuild] = {
    if (!build.isPersonal && build.matchBranch(setting.branchMask))
      Some(build)
    else {
      Option(build.getPreviousFinished) match {
        case Some(previous) => filterMatchBuild(setting, previous)
        case None           => None
      }
    }
  }

  def findPreviousBuild(
      projectManager: ProjectManager,
      setting: BuildSetting
  ): Option[SFinishedBuild] = {
    val buildTypes =
      projectManager.findBuildTypes(Vector(setting.buildTypeId).asJava)
    val foundBuildType = buildTypes.asScala.headOption

    foundBuildType
      .flatMap(buildType => Option(buildType.getLastChangesFinished))
      .flatMap(build => filterMatchBuild(setting, build))
  }

  def detectDestination(
      setting: BuildSetting,
      user: => SUser
  ): Option[Destination] = setting.slackChannel.isEmpty match {
    case true if setting.notifyCommitter =>
      Some(SlackUser(user.getEmail))
    case false =>
      Some(SlackChannel(setting.slackChannel))
    case _ =>
      None
  }

  case class HandlerException(message: String) extends Exception(message)
}
