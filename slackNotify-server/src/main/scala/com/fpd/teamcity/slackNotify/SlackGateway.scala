package com.fpd.teamcity.slackNotify

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackAttachment, SlackMessageHandle, SlackSession}

import scala.language.implicitConversions
import scala.util.Try

object SlackGateway {

  sealed trait Destination

  case class SlackUser(email: String) extends Destination

  case class SlackChannel(name: String) extends Destination

  case class SlackMessage(message: String, attachment: Option[SlackAttachment] = None)

  implicit def stringToSlackMessage(message: String): SlackMessage = SlackMessage(message)

  type MessageSent = SlackMessageHandle[SlackMessageReply]

  def sessionByConfig(config: ConfigManager.Config): Option[SlackSession] = {
    // TODO: cache connection
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey)
    Try(session.connect()).map(_ ⇒ session).toOption
  }
}

class SlackGateway(val configManager: ConfigManager) {

  import SlackGateway._

  def session: Option[SlackSession] = configManager.config.flatMap(sessionByConfig)

  def sendMessage(destination: Destination, message: SlackMessage): Option[MessageSent] = session.flatMap { x ⇒
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
