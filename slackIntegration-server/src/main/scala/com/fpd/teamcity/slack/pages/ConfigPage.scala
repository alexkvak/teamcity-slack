package com.fpd.teamcity.slack.pages

import com.fpd.teamcity.slack.{
  ConfigManager,
  PermissionManager,
  Resources,
  Strings
}
import jetbrains.buildServer.controllers.admin.AdminPage
import jetbrains.buildServer.web.CSRFFilter
import jetbrains.buildServer.web.openapi.{
  Groupable,
  PagePlaces,
  PluginDescriptor
}

import java.util
import javax.servlet.http.HttpServletRequest

class ConfigPage(
    extension: ConfigManager,
    pagePlaces: PagePlaces,
    descriptor: PluginDescriptor,
    val permissionManager: PermissionManager
) extends AdminPage(
      pagePlaces,
      Strings.tabId,
      descriptor.getPluginResourcesPath(ConfigPage.includeUrl),
      Strings.label
    )
    with SlackExtension {

  register()

  addJsFile(descriptor.getPluginResourcesPath("js/slack-notifier-config.js"))

  override def fillModel(
      model: util.Map[String, AnyRef],
      request: HttpServletRequest
  ): Unit = {
    import com.fpd.teamcity.slack.Helpers.Implicits._

    import scala.jdk.CollectionConverters._

    model.putAll(extension.details.view.mapValues(_.getOrElse("")).toMap.asJava)
    model.put("error", request.param("error").getOrElse(""))
    model.put("saveConfigSubmitUrl", Resources.configPage.controllerUrl)
    model.put(
      "tcCsrfToken",
      request.getSession.getAttribute(CSRFFilter.ATTRIBUTE)
    )
  }

  override def getGroup: String = Groupable.SERVER_RELATED_GROUP
}

object ConfigPage {
  private def includeUrl: String = Resources.configPage.view
}
