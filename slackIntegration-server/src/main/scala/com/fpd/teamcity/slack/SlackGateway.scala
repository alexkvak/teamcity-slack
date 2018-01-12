package com.fpd.teamcity.slack

import java.net.Proxy
import java.util.concurrent.TimeUnit

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.{ParsedSlackReply, SlackMessageReply}
import com.ullink.slack.simpleslackapi.{SlackChatConfiguration, SlackMessageHandle, SlackSession, SlackAttachment ⇒ ApiSlackAttachment}
import jetbrains.buildServer.serverSide.TeamCityProperties

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

object SlackGateway {

  sealed trait Destination

  case class SlackUser(email: String) extends Destination {
    override def toString: String = email
  }

  case class SlackChannel(name: String) extends Destination {
    override def toString: String = s"#$name"
  }

  case class SlackMessage(message: String, attachment: Option[ApiSlackAttachment] = None) {
    lazy val isEmpty: Boolean = message.isEmpty && attachment.isEmpty
  }

  case class SlackAttachment(text: String, color: String)

  implicit def stringToSlackMessage(message: String): SlackMessage = SlackMessage(message)

  type MessageSent = SlackMessageHandle[SlackMessageReply]

  implicit def attachmentToSlackMessage(attachment: SlackAttachment): SlackMessage = {
    val apiSlackAttachment = new ApiSlackAttachment()
    apiSlackAttachment.setColor(attachment.color)
    apiSlackAttachment.addMarkdownIn("fields")
    apiSlackAttachment.addField("", attachment.text, false)

    SlackMessage("", Some(apiSlackAttachment))
  }

  val networkTimeout = 10L

  def getStringProperty(key: String): Option[String] = {
    implicit class RichString(opt: Option[String]) {
      def trimEmptyString: Option[String] = opt.map(_.trim).filterNot(_.isEmpty)
    }

    Try(Option(System.getProperty(key))).getOrElse(None)
      .trimEmptyString
      .orElse(
        Option(TeamCityProperties.getPropertyOrNull(s"teamcity.$key"))
      )
      .trimEmptyString
  }

  def getIntProperty(key: String): Int =
    Try(System.getProperty(key).toInt)
      .getOrElse(
        TeamCityProperties.getInteger(s"teamcity.$key")
      )
}

class SlackGateway(val configManager: ConfigManager, logger: Logger) {

  import SlackGateway._

  var sessions = Map.empty[String, SlackSession]

  lazy private val proxyHost = getStringProperty("https.proxyHost")
  lazy private val proxyPort = getIntProperty("https.proxyPort")
  lazy private val proxyLogin = getStringProperty("https.proxyLogin")
  lazy private val proxyPassword = getStringProperty("https.proxyPassword")

  def session: Option[SlackSession] = configManager.config.flatMap(x ⇒ sessionByConfig(x).toOption)

  def sessionByConfig(config: ConfigManager.Config): Try[SlackSession] = sessions.get(config.oauthKey).filter(_.isConnected) match {
    case Some(x) ⇒ Success(x)
    case _ ⇒
      val session = if (proxyHost.isDefined)
        SlackSessionFactory
          .getSlackSessionBuilder(config.oauthKey)
          .withAutoreconnectOnDisconnection(true)
          .withConnectionHeartbeat(0, null)
          .withProxy(Proxy.Type.HTTP, proxyHost.get, proxyPort, proxyLogin.orNull, proxyPassword.orNull)
          .build()
      else
        SlackSessionFactory.createWebSocketSlackSession(config.oauthKey)

      val option = Try(session.connect()).map(_ ⇒ session)
      option.foreach(s ⇒ sessions = sessions + (config.oauthKey → s))
      option
  }

  def sendMessage(destination: Destination, message: SlackMessage): Option[MessageSent] =
    if (message.isEmpty) {
      logger.log("Empty message")
      None
    } else {
      implicit val ec = scala.concurrent.ExecutionContext.global
      val future = Future.fromTry {
        val handle = Try(sendMessageInternal(destination, message))
        handle.foreach(x ⇒ x.foreach(_.waitForReply(networkTimeout, TimeUnit.SECONDS)))
        handle
      }
      future.onComplete {
        case Failure(exception) ⇒ logger.log(exception.toString)
        case _ ⇒
      }
      processResult(destination, Await.result(future, networkTimeout seconds))
    }

  private def channelChatConfiguration =
    SlackChatConfiguration.getConfiguration.withName(configManager.senderName.getOrElse(Strings.channelMessageOwner))

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

  private def parseReplyError(reply: ParsedSlackReply): Option[String] =
    if (!reply.isOk) {
      Some(reply.getErrorMessage)
    } else None
}
