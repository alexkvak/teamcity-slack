package com.fpd.teamcity.slack

import java.util.concurrent.TimeUnit

import com.ullink.slack.simpleslackapi.impl.{SlackChatConfiguration, SlackSessionFactory}
import com.ullink.slack.simpleslackapi.replies.{GenericSlackReply, SlackMessageReply, SlackReply}
import com.ullink.slack.simpleslackapi.{SlackMessageHandle, SlackSession, SlackAttachment ⇒ ApiSlackAttachment}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{implicitConversions, postfixOps}
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
    apiSlackAttachment.addMarkdownIn("text")

    SlackMessage("", Some(apiSlackAttachment))
  }

  val networkTimeout = 10L
  lazy val channelChatConfiguration: SlackChatConfiguration = SlackChatConfiguration.getConfiguration.withName(Strings.channelMessageOwner)
}

class SlackGateway(val configManager: ConfigManager, logger: Logger) {

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
    if (message.isEmpty) {
      logger.log("Empty message")
      None
    } else {
      implicit val ec = scala.concurrent.ExecutionContext.global
      val future = Future {
        val handle = sendMessageInternal(destination, message)
        handle.foreach(_.waitForReply(networkTimeout, TimeUnit.SECONDS))
        handle
      }
      processResult(destination, Await.result(future, networkTimeout seconds))
    }

  private def sendMessageInternal(destination: Destination, message: SlackMessage): Option[MessageSent] = session.flatMap { x ⇒
    destination match {
      case SlackChannel(channel) ⇒ Option(x.findChannelByName(channel)).map { channel ⇒
        x.sendMessage(channel, message.message, message.attachment.orNull, channelChatConfiguration)
      }
      case SlackUser(email) ⇒ Option(x.findUserByEmail(email)).map { user ⇒
        x.sendMessageToUser(user, message.message, message.attachment.orNull)
      }
    }
  }

  private def processResult(destination: Destination, result: Option[MessageSent]): Option[MessageSent] = {
    val dest = destination match {
      case SlackChannel(channel) ⇒ s"channel #$channel"
      case SlackUser(email) ⇒ s"user $email"
    }

    result match {
      case Some(sent) if sent.getReply != null ⇒
        parseReplyError(sent.getReply) match {
          case Some(error) ⇒
            logger.log(s"Message to $dest wasn't sent. Reason: $error")
            None
          case _ ⇒
            logger.log(s"Message sent to $dest")
            result
        }
      case _ ⇒
        logger.log(s"Message to $dest wasn't sent. Reason: timeout")
        None
    }
  }

  //noinspection TypeCheckCanBeMatch
  private def parseReplyError(reply: SlackReply): Option[String] =
    if (reply.isInstanceOf[GenericSlackReply] && !reply.asInstanceOf[GenericSlackReply].getPlainAnswer.get("ok").asInstanceOf[Boolean]) {
      Some(reply.asInstanceOf[GenericSlackReply].getPlainAnswer.toJSONString)
  } else None
}
