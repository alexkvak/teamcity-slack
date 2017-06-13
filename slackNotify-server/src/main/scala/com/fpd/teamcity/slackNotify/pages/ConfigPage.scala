package com.fpd.teamcity.slackNotify.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.{ConfigManager, Strings}
import jetbrains.buildServer.controllers.admin.AdminPage
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.openapi.{Groupable, PagePlaces, PluginDescriptor}

class ConfigPage(extension: ConfigManager, pagePlaces: PagePlaces, descriptor: PluginDescriptor)
  extends AdminPage(pagePlaces, Strings.tabId, descriptor.getPluginResourcesPath(ConfigPage.includeUrl), Strings.label) {

  register()

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest): Unit = {
    import collection.JavaConverters._

    model.putAll(extension.details.mapValues(_.getOrElse("")).asJava)
    model.put("error", Option(request.getParameter("error")).getOrElse(""))
  }

  override def isAvailable(request: HttpServletRequest): Boolean =
    super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS)

  override def getGroup: String = Groupable.SERVER_RELATED_GROUP
}

object ConfigPage {
  private def includeUrl: String = "configPage.jsp"
}
