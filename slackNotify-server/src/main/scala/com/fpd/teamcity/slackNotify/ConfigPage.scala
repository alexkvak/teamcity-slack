package com.fpd.teamcity.slackNotify

import java.util
import javax.servlet.http.HttpServletRequest

import jetbrains.buildServer.controllers.admin.AdminPage
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.openapi.{Groupable, PagePlaces, PluginDescriptor}

class ConfigPage(extension: ConfigManager, pagePlaces: PagePlaces, descriptor: PluginDescriptor)
  extends AdminPage(pagePlaces, "SlackNotifier", descriptor.getPluginResourcesPath("configPage.jsp"), "Slack Notifier") {

  register()

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest): Unit = {
    import collection.JavaConverters._

    model.putAll(extension.details.mapValues(_.getOrElse("")).asJava)
  }

  override def isAvailable(request: HttpServletRequest): Boolean =
    super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS)

  override def getGroup: String = Groupable.SERVER_RELATED_GROUP
}
