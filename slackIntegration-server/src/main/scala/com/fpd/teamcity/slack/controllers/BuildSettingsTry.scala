package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.Helpers._
import com.fpd.teamcity.slack.SlackGateway.SlackChannel
import com.fpd.teamcity.slack.{ConfigManager, MessageBuilder, Resources, SlackGateway}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.serverSide.{BuildHistory, WebLinks}
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingsTry(buildHistory: BuildHistory,
                       configManager: ConfigManager,
                       webLinks: WebLinks,
                       gateway: SlackGateway,
                       controllerManager: WebControllerManager,
                       implicit val descriptor: PluginDescriptor
                      )
  extends BaseController with SlackController {

  controllerManager.registerController(Resources.buildSettingTry.url, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val result = for {
      id ← request.param("id")
      setting ← configManager.buildSetting(id)
      build ← buildHistory.getEntries(false).asScala.find(_.getBuildTypeId == setting.buildTypeId)
      _ ← gateway.sendMessage(SlackChannel(setting.slackChannel), MessageBuilder(setting.messageTemplate).compile(build, webLinks))
    } yield {
      s"Message sent to #${setting.slackChannel}"
    }

    ajaxView(result getOrElse "Something went wrong")
  }
}
