package com.fpd.teamcity.slackNotify

import com.ullink.slack.simpleslackapi.{SlackChannel, SlackMessageHandle, SlackSession}
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply

import scala.util.Try

object SlackGateway {
  case class Destination(session: SlackSession, channel: SlackChannel)

  type MessageSent = SlackMessageHandle[SlackMessageReply]

  def sessionByConfig(config: ConfigManager.Config): Option[SlackSession] = {
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey)
    Try(session.connect()).map(_ ⇒ session).toOption
  }
}

class SlackGateway(val configManager: ConfigManager) {
  import SlackGateway._

  def session: Option[SlackSession] = configManager.config.flatMap(sessionByConfig)

  def sendMessage(channel: String, message: String): Option[MessageSent] = session.flatMap { x ⇒
    Option(x.findChannelByName(channel)).map { channel ⇒
      x.sendMessage(channel, message)
    }
  }
}
