package com.fpd.teamcity.slackNotify

import com.ullink.slack.simpleslackapi.{SlackChannel, SlackMessageHandle, SlackSession}
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply

object SlackGateway {
  case class Destination(session: SlackSession, channel: SlackChannel)

  type MessageSent = SlackMessageHandle[SlackMessageReply]

  def destinationByConfig(config: Config): Option[Destination] = {
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey)
    session.connect()
    Option(session.findChannelByName(config.channel)).map(Destination(session, _))
  }
}

class SlackGateway(implicit val configManager: ConfigManager) {
  import SlackGateway._

  def destination: Option[Destination] = configManager.config.flatMap(destinationByConfig)

  def sendMessage(message: String): Option[MessageSent] = {
    destination.map(x ⇒ x.session.sendMessage(x.channel, message))
  }
}
