package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.SlackGateway.{Destination, SlackChannel, SlackUser}
import com.fpd.teamcity.slack._
import jetbrains.buildServer.serverSide.{BuildHistory, SFinishedBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import jetbrains.buildServer.web.util.SessionUser
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

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

  controllerManager.registerController(Resources.buildSettingTry.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      id ← request.param("id")
      setting ← configManager.buildSetting(id)
      build ← findPreviousBuild(buildHistory, setting)
    } yield {
      detectDestination(setting, SessionUser.getUser(request)) match {
        case Some(dest) ⇒
          gateway.sendMessage(dest, messageBuilderFactory.createForBuild(build).compile(setting.messageTemplate, Some(setting)))
          messageSent(dest.toString)
        case _ ⇒
          unknownDestination
      }
    }

    ajaxView(result getOrElse "Something went wrong")
  }

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
}
