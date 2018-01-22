package com.fpd.teamcity.slack

import java.net.Proxy
import java.util.concurrent.TimeUnit

import com.fpd.teamcity.slack.Strings.SlackGateway._
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.{ParsedSlackReply, SlackMessageReply}
import com.ullink.slack.simpleslackapi.{SlackChatConfiguration, SlackMessageHandle, SlackSession, SlackAttachment ⇒ ApiSlackAttachment}
import jetbrains.buildServer.serverSide.TeamCityProperties

import scala.concurrent.Future
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

  type MessageSent = Try[SlackMessageHandle[SlackMessageReply]]

  implicit def attachmentToSlackMessage(attachment: SlackAttachment): SlackMessage = {
    val apiSlackAttachment = new ApiSlackAttachment()
    apiSlackAttachment.setColor(attachment.color)
    apiSlackAttachment.addMarkdownIn("fields")
    apiSlackAttachment.addField("", attachment.text, false)

    SlackMessage("", Some(apiSlackAttachment))
  }

  val networkTimeout = 10L

  implicit class RichString(val opt: Option[String]) extends AnyVal {
    def trimEmptyString: Option[String] = opt.map(_.trim).filterNot(_.isEmpty)
  }

  def getStringProperty(key: String): Option[String] = {
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

  case class SendMessageError(message: String) extends Exception(message)
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

  def sendMessage(destination: Destination, message: SlackMessage): Future[MessageSent] =
    if (message.isEmpty) {
      logger.log("Empty message")
      Future.successful(Failure(SendMessageError("Empty message")))
    } else {
      implicit val ec = scala.concurrent.ExecutionContext.global
       Future {
        val handle = sendMessageInternal(destination, message)
        handle.foreach(_.waitForReply(networkTimeout, TimeUnit.SECONDS))
        handle
      } transform (
         result ⇒ processResult(destination, result),
         exception ⇒ {
           logger.log(exception.toString)
           exception
         }
       )
    }

  private def channelChatConfiguration =
    SlackChatConfiguration.getConfiguration.withName(configManager.senderName.getOrElse(Strings.channelMessageOwner))

  private def sendMessageInternal(destination: Destination, message: SlackMessage): MessageSent = session match {
    case Some(x) ⇒
      destination match {
        case SlackChannel(channelName) ⇒
          Option(x.findChannelByName(channelName)) match {
            case Some(channel) ⇒
              Try(x.sendMessage(channel, message.message, message.attachment.orNull, channelChatConfiguration))
            case _ ⇒
              Failure(SendMessageError(channelNotFound(channelName)))
          }
        case SlackUser(email) ⇒
          Option(x.findUserByEmail(email)) match {
            case Some(user) ⇒
              Try(x.sendMessageToUser(user, message.message, message.attachment.orNull))
            case _ ⇒
              Failure(SendMessageError(userNotFound(email)))
        }
        case _ ⇒
          Failure(SendMessageError(unknownDestination))
      }
    case _ ⇒
      Failure(SendMessageError(emptySession))
  }

  private def processResult(destination: Destination, result: MessageSent): MessageSent = {
    result match {
      case Success(sent) if sent.getReply != null ⇒
        parseReplyError(sent.getReply) match {
          case Some(error) ⇒
            val message = failedToSendToDestination(destination, error)
            logger.log(message)
            Failure(SendMessageError(message))
          case _ ⇒
            logger.log(messageSent(destination))
            Success(sent)
        }
      case x @ Failure(reason) ⇒
        logger.log(reason.getMessage)
        x
    }
  }

  private def parseReplyError(reply: ParsedSlackReply): Option[String] =
    if (!reply.isOk) {
      Some(reply.getErrorMessage)
    } else None
}
