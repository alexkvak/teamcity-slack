package com.fpd.teamcity.slackNotify.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.{ConfigManager, Resources, Strings}
import jetbrains.buildServer.controllers.admin.AdminPage
import jetbrains.buildServer.web.openapi.{Groupable, PagePlaces, PluginDescriptor}

class ConfigPage(extension: ConfigManager, pagePlaces: PagePlaces, descriptor: PluginDescriptor)
  extends AdminPage(
    pagePlaces,
    Strings.tabId,
    descriptor.getPluginResourcesPath(ConfigPage.includeUrl),
    Strings.label) with SlackPage {

  register()

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest): Unit = {
    import collection.JavaConverters._
    import com.fpd.teamcity.slackNotify.Helpers._

    model.putAll(extension.details.mapValues(_.getOrElse("")).asJava)
    model.put("error", request.param("error").getOrElse(""))
  }

  override def getGroup: String = Groupable.SERVER_RELATED_GROUP
}

object ConfigPage {
  private def includeUrl: String = Resources.configPage.view
}
