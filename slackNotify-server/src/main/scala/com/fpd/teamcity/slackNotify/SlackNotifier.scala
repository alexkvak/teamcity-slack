package com.fpd.teamcity.slackNotify

import java.util

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import jetbrains.buildServer.notification.{Notificator, NotificatorAdapter, NotificatorRegistry}
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.users.SUser

object SlackNotifier {
  private def sendMessage(message: String)(implicit config: ConfigManager) = {
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey.getOrElse(""))
    session.connect()
    val channel = session.findChannelByName("#general") //make sure bot is a member of the channel.
    session.sendMessage(channel, message)
  }
}

class SlackNotifier(notificatorRegistry: NotificatorRegistry, implicit val config: ConfigManager) extends NotificatorAdapter with Notificator {
  import SlackNotifier._

  notificatorRegistry.register(this)

  override def notifyBuildStarted(build: SRunningBuild, users: util.Set[SUser]): Unit =
    sendMessage(build.getBuildDescription)

  override def getNotificatorType: String = "slackNotifier"
}
