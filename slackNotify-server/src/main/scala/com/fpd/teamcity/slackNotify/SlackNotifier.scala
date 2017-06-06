package com.fpd.teamcity.slackNotify

import java.util

import jetbrains.buildServer.notification.{NotificatorAdapter, NotificatorRegistry}
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.users.SUser

class SlackNotifier(notificatorRegistry: NotificatorRegistry, gateway: SlackGateway) extends NotificatorAdapter {

  notificatorRegistry.register(this)

  override def notifyBuildStarted(build: SRunningBuild, users: util.Set[SUser]): Unit =
    gateway.sendMessage(s"${build.getAgentName} started")

  override def getNotificatorType: String = "slackNotifier"

  override def getDisplayName: String = "Slack Notifier"
}
