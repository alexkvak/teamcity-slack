package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.{ConfigManager, Resources}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.ContentSecurityPolicyConfig
import jetbrains.buildServer.web.openapi._
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization
import org.springframework.web.servlet.ModelAndView

class SakuraUIPluginController(
                                descriptor: PluginDescriptor,
                                places: PagePlaces,
                                controllerManager: WebControllerManager,
                                contentSecurityPolicyConfig: ContentSecurityPolicyConfig,
                                config: ConfigManager
                              ) extends BaseController {
  private val PLUGIN_NAME = "SakuraUI-Plugin"
  private val BUNDLE_DEV_URL = "http://localhost:8080"

  private val myPluginDescriptor = descriptor

  val url = "/reactPlugin.html"

  val pageExtension = new SimplePageExtension(places)

  pageExtension.setPluginName(PLUGIN_NAME)
  pageExtension.setPlaceId(PlaceId.ALL_PAGES_FOOTER)
  pageExtension.setIncludeUrl(url)
  pageExtension.register()

  controllerManager.registerController(url, this)
  contentSecurityPolicyConfig.addDirectiveItems("script-src", BUNDLE_DEV_URL)

  override protected def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    request.param("buildTypeId") match {
      case Some(buildTypeId) ⇒
        val formats = new DefaultFormats {
          override def alwaysEscapeUnicode: Boolean = true
        } + new EnumNameSerializer(BuildSettingFlag)
        val message = Serialization.write(config.buildSettingList(buildTypeId))(formats)

        val ajaxView = descriptor.getPluginResourcesPath(Resources.ajaxView.view)

        val modelAndView = new ModelAndView(ajaxView)
        modelAndView.getModel.put("message", message)
        modelAndView
      case _ ⇒
        val mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("react-plugin.jsp"))
        mv.getModel.put("BUNDLE_DEV_URL", BUNDLE_DEV_URL)
        mv
    }

  }
}
