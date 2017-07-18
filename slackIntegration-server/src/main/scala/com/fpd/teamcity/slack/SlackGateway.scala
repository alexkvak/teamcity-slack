package com.fpd.teamcity.slack

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackAttachment ⇒ ApiSlackAttachment, SlackMessageHandle, SlackSession}

import scala.language.implicitConversions
import scala.util.Try

object SlackGateway {

  sealed trait Destination

  case class SlackUser(email: String) extends Destination

  case class SlackChannel(name: String) extends Destination

  case class SlackMessage(message: String, attachment: Option[ApiSlackAttachment] = None) {
    def isEmpty: Boolean = message.isEmpty && (attachment.isEmpty || attachment.get.text.isEmpty)
  }

  case class SlackAttachment(text: String, color: String)

  implicit def stringToSlackMessage(message: String): SlackMessage = SlackMessage(message)

  type MessageSent = SlackMessageHandle[SlackMessageReply]

  implicit def attachmentToSlackMessage(attachment: SlackAttachment): SlackMessage = {
    val apiSlackAttachment = new ApiSlackAttachment()
    apiSlackAttachment.setText(attachment.text)
    apiSlackAttachment.setColor(attachment.color)

    SlackMessage("", Some(apiSlackAttachment))
  }
}

class SlackGateway(val configManager: ConfigManager) {

  import SlackGateway._

  var sessions = Map.empty[String, SlackSession]

  def session: Option[SlackSession] = configManager.config.flatMap(sessionByConfig)

  def sessionByConfig(config: ConfigManager.Config): Option[SlackSession] = sessions.get(config.oauthKey).filter(_.isConnected).orElse {
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey)
    val option = Try(session.connect()).map(_ ⇒ session).toOption
    option.foreach(s ⇒ sessions = sessions + (config.oauthKey → s))
    option
  }

  def sendMessage(destination: Destination, message: SlackMessage): Option[MessageSent] =
    if (message.isEmpty) None else sendMessageInternal(destination, message)

  private def sendMessageInternal(destination: Destination, message: SlackMessage): Option[MessageSent] = session.flatMap { x ⇒
    destination match {
      case SlackChannel(channel) ⇒ Option(x.findChannelByName(channel)).map { channel ⇒
        x.sendMessage(channel, message.message, message.attachment.orNull)
      }
      case SlackUser(email) ⇒ Option(x.findUserByEmail(email)).map { user ⇒
        x.sendMessageToUser(user, message.message, message.attachment.orNull)
      }
    }
  }
}
