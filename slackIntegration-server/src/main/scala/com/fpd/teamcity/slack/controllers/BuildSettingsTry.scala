package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.SlackGateway.SlackChannel
import com.fpd.teamcity.slack._
import jetbrains.buildServer.serverSide.BuildHistory
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
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

  controllerManager.registerController(Resources.buildSettingTry.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      id ← request.param("id")
      setting ← configManager.buildSetting(id)
      build ← buildHistory.getEntries(false).asScala.find(_.getBuildTypeId == setting.buildTypeId)
      _ ← gateway.sendMessage(SlackChannel(setting.slackChannel), messageBuilderFactory.createForBuild(build).compile(setting.messageTemplate, Some(setting)))
    } yield {
      s"Message sent to #${setting.slackChannel}"
    }

    ajaxView(result getOrElse "Something went wrong")
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("id").exists(id ⇒ permissionManager.settingAccessPermitted(request, id))
}
