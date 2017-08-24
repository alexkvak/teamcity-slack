package com.fpd.teamcity.slack

import java.util

import jetbrains.buildServer.notification.{NotificatorAdapter, NotificatorRegistry}
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.users.{NotificatorPropertyKey, SUser}

import scala.language.implicitConversions

class SlackNotifier(notificatorRegistry: NotificatorRegistry) extends NotificatorAdapter {
  import SlackNotifier._

  notificatorRegistry.register(this)

  override def notifyBuildStarted(build: SRunningBuild, users: util.Set[SUser]): Unit = Unit

  override def getNotificatorType: String = notificatorType

  override def getDisplayName: String = displayName
}

object SlackNotifier {
  implicit private def propertyNameToPropertyKey(propertyName: String): NotificatorPropertyKey =
    new NotificatorPropertyKey(notificatorType, propertyName)

  private def notificatorType = Strings.tabId

  private def displayName = Strings.label
}
