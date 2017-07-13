package com.fpd.teamcity.slack

import java.util

import com.fpd.teamcity.slack.SlackGateway.SlackChannel
import jetbrains.buildServer.notification.{NotificatorAdapter, NotificatorRegistry}
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.users.{NotificatorPropertyKey, SUser}

import scala.language.implicitConversions

class SlackNotifier(notificatorRegistry: NotificatorRegistry, gateway: SlackGateway) extends NotificatorAdapter {
  import SlackNotifier._

  notificatorRegistry.register(this)

  override def notifyBuildStarted(build: SRunningBuild, users: util.Set[SUser]): Unit = users.forEach { user ⇒
    Option(user.getPropertyValue(channelPropertyKey)).foreach { channel ⇒
      gateway.sendMessage(SlackChannel(channel), s"${build.getAgentName} started")
    }
  }
  override def getNotificatorType: String = notificatorType

  override def getDisplayName: String = displayName
}

object SlackNotifier {
  implicit private def propertyNameToPropertyKey(propertyName: String): NotificatorPropertyKey =
    new NotificatorPropertyKey(notificatorType, propertyName)

  private def notificatorType: String = Strings.tabId

  private def displayName: String = Strings.label

  private def channelPropertyKey: String = "ChannelName"
}
