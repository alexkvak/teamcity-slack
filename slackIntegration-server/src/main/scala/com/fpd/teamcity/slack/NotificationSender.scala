package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{Destination, MessageSent, SlackChannel, SlackUser, attachmentToSlackMessage}
import jetbrains.buildServer.serverSide.{SBuild, SQueuedBuild}

import scala.collection.mutable
import scala.concurrent.Future

trait NotificationSender {

  val configManager: ConfigManager
  val gateway: SlackGateway
  val messageBuilderFactory: MessageBuilderFactory

  import Helpers.Implicits._

  type SendResult = Vector[Future[MessageSent]]

  private def sendAsAttachment = configManager.sendAsAttachment.exists(x ⇒ x)

  def send(build: SBuild, flags: Set[BuildSettingFlag]): Future[Vector[MessageSent]] = {
    val settings = prepareSettings(build, flags)

    lazy val emails = build.committeeEmails
    lazy val messageBuilder = messageBuilderFactory.createForBuild(build)
    lazy val sendPersonal = shouldSendPersonal(build)

    val result = settings.foldLeft(Vector(): SendResult) { (acc, setting) ⇒
      val attachment = messageBuilder.compile(setting.messageTemplate, setting)
      val destinations = mutable.Set.empty[Destination]
      if (build.isPersonal) {
        // If build is personal we need inform only build's owner if needed
        val email = build.getOwner.getEmail
        if (sendPersonal && email.length > 0) {
          destinations += SlackUser(email)
        }
      } else {
        if (setting.slackChannel.nonEmpty) {
          destinations += SlackChannel(setting.slackChannel)
        }

        /**
          * if build fails all committees should receive the message
          * if personal notification explicitly enabled in build settings let's notify all committees
          */
        if (setting.notifyCommitter || sendPersonal) {
          emails.foreach { email ⇒
            destinations += SlackUser(email)
          }
        }
      }

      acc ++ destinations.toVector.map(x ⇒
        gateway.sendMessage(x, attachmentToSlackMessage(attachment, sendAsAttachment))
      )
    }

    implicit val ec = scala.concurrent.ExecutionContext.global
    Future.sequence(result)
  }

  def send(build: SQueuedBuild, flags: Set[BuildSettingFlag]): Future[Vector[MessageSent]] = {
    val settings = prepareSettings(build, flags)

    lazy val messageBuilder = messageBuilderFactory.createForBuild(build)

    val result = settings.foldLeft(Vector(): SendResult) { (acc, setting) ⇒
      val attachment = messageBuilder.compile(setting.messageTemplate, setting)
      val destinations = mutable.Set.empty[Destination]
      if (!build.isPersonal && setting.slackChannel.nonEmpty) {
          destinations += SlackChannel(setting.slackChannel)
      }

      acc ++ destinations.toVector.map(x ⇒
        gateway.sendMessage(x, attachmentToSlackMessage(attachment, sendAsAttachment))
      )
    }

    implicit val ec = scala.concurrent.ExecutionContext.global
    Future.sequence(result)
  }

  def shouldSendPersonal(build: SBuild): Boolean = build.getBuildStatus.isFailed && configManager.personalEnabled.exists(x ⇒ x)

  def prepareSettings(build: SBuild, flags: Set[BuildSettingFlag]): Iterable[BuildSetting] =
    configManager.buildSettingList(build.getBuildTypeId).values.filter { x ⇒
      x.pureFlags.intersect(flags).nonEmpty && build.matchBranch(x.branchMask)
    }

  def prepareSettings(build: SQueuedBuild, flags: Set[BuildSettingFlag]): Iterable[BuildSetting] =
    configManager.buildSettingList(build.getBuildTypeId).values.filter { x ⇒
      x.pureFlags.intersect(flags).nonEmpty && build.matchBranch(x.branchMask)
    }
}
