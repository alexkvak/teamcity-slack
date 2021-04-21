package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.Strings.SlackGateway._
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.{
  ConversationsListRequest,
  ConversationsOpenRequest
}
import com.slack.api.methods.request.users.UsersLookupByEmailRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.{
  AsyncMethodsClient,
  MethodsClient,
  SlackApiTextResponse
}
import com.slack.api.model.{Attachment, ConversationType, Field, User}
import com.slack.api.{Slack, SlackConfig}
import jetbrains.buildServer.serverSide.TeamCityProperties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
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

  case class SlackMessage(
      message: String,
      attachment: Option[Attachment] = None
  ) {
    lazy val isEmpty: Boolean = message.isEmpty && attachment.isEmpty
    def attachmentsList: Seq[Attachment] = attachment match {
      case Some(attach) => Seq(attach)
      case _            => Seq()
    }
  }

  case class SlackAttachment(text: String, color: String, emoji: String)

  implicit def stringToSlackMessage(message: String): SlackMessage =
    SlackMessage(message)

  def attachmentToSlackMessage(
      attachment: SlackAttachment,
      asAttachment: Boolean
  ): SlackMessage = if (asAttachment) {
    val apiSlackAttachment = new Attachment()
    apiSlackAttachment.setColor(attachment.color)
    apiSlackAttachment.setMrkdwnIn(Seq("fields").asJava)
    apiSlackAttachment.setFields(
      Seq(new Field("", attachment.text, false)).asJava
    )

    SlackMessage("", Some(apiSlackAttachment))
  } else {
    SlackMessage(s"${attachment.emoji} ${attachment.text}")
  }

  val networkTimeout = 10L

  implicit class RichString(val opt: Option[String]) extends AnyVal {
    def trimEmptyString: Option[String] = opt.map(_.trim).filterNot(_.isEmpty)
  }

  def getStringProperty(key: String): Option[String] = {
    Try(Option(System.getProperty(key)))
      .getOrElse(None)
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

  case class SlackApiError(message: String) extends Exception(message)

  private def prepareConfig: SlackConfig = {
    val config = new SlackConfig

    val proxyHost = getStringProperty("https.proxyHost")
    val proxyPort = getIntProperty("https.proxyPort")

    val proxyUrl = proxyHost.map(host =>
      if (proxyPort > 0) s"http://$host:$proxyPort"
      else s"http://$host"
    )

    proxyUrl.foreach(x => config.setProxyUrl(x))

    for {
      url <- proxyUrl
      proxyLogin <- getStringProperty("https.proxyLogin")
      proxyPassword <- getStringProperty("https.proxyPassword")
    } yield config.setProxyUrl(s"http://$proxyLogin:$proxyPassword@$url")

    config
  }

  private def processResult[T <: SlackApiTextResponse](
      result: Future[T]
  ): Future[T] = result.flatMap(response => {
    if (response.isOk) Future.successful(response)
    else Future.failed(SlackApiError(response.getError))
  })
}

class SlackGateway(val configManager: ConfigManager, logger: Logger) {

  import SlackGateway._

  private val slack = Slack.getInstance(prepareConfig)

  var methodsClients = Map.empty[String, AsyncMethodsClient]

  private def methods: Option[AsyncMethodsClient] =
    configManager.config.flatMap(x => sessionByConfig(x))

  private def checkConnection(methods: MethodsClient): Boolean = {
    val request =
      ConversationsListRequest
        .builder()
        .limit(1)
        .build()

    Try(methods.conversationsList(request)) match {
      case Success(value) if value.isOk => true
      case Success(value) =>
        logger.log(value.getError)
        false
      case Failure(exception) =>
        logger.log(exception.getMessage)
        false
    }
  }

  def sessionByConfig(
      config: ConfigManager.Config
  ): Option[AsyncMethodsClient] =
    methodsClients.get(config.oauthKey) match {
      case None =>
        val syncMethods = slack.methods(config.oauthKey)

        if (checkConnection(syncMethods)) {
          val methods = slack.methodsAsync(config.oauthKey)
          methodsClients = methodsClients + (config.oauthKey -> methods)

          Some(methods)
        } else {
          None
        }
      case x => x
    }

  def sendMessage(
      destination: Destination,
      message: SlackMessage
  ): Future[Unit] =
    if (message.isEmpty) {
      logger.log("Empty message")
      Future.failed(SendMessageError("Empty message"))
    } else {
      methods match {
        case Some(client) =>
          val response = sendMessageInternal(client, destination, message)

          processResult(response).transform(
            _ => logger.log(messageSent(destination)),
            throwable => {
              val message =
                failedToSendToDestination(destination, throwable.getMessage)
              logger.log(message)

              SendMessageError(message)
            }
          )
        case None =>
          Future.failed(SendMessageError(emptySession))
      }
    }

  def getUserByEmail(email: String): Option[User] = methods.flatMap { client =>
    {
      val request = UsersLookupByEmailRequest.builder().email(email).build()

      val response = client.usersLookupByEmail(request).asScala

      val user = processResult(response).map(_.getUser)

      Try(Await.result(user, 10 seconds)).fold(
        exception => {
          logger.log(exception.getMessage)
          None
        },
        response => Some(response)
      )
    }
  }

  def isChannelExists(channel: String): Option[Boolean] = methods.map {
    client =>
      {
        val types =
          Seq(ConversationType.PUBLIC_CHANNEL, ConversationType.PRIVATE_CHANNEL)

        val request =
          ConversationsListRequest
            .builder()
            .limit(1000)
            .excludeArchived(true)
            .types(types.asJava)
            .build()

        val response = client.conversationsList(request).asScala

        val channels = processResult(response).map(_.getChannels.asScala)

        Try(Await.result(channels, 30 seconds)).fold(
          exception => {
            logger.log(exception.getMessage)
            false
          },
          response => response.exists(_.getName == channel)
        )
      }
  }

  def startConversation(userId: String): Option[String] = methods.flatMap {
    client =>
      {
        val request =
          ConversationsOpenRequest.builder().users(List(userId).asJava).build()

        val response = client.conversationsOpen(request).asScala

        val channelId = processResult(response).map(_.getChannel.getId)

        Try(Await.result(channelId, 30 seconds)).fold(
          exception => {
            logger.log(exception.getMessage)
            None
          },
          response => Some(response)
        )
      }
  }

  private def sendMessageInternal(
      client: AsyncMethodsClient,
      destination: Destination,
      message: SlackMessage
  ): Future[ChatPostMessageResponse] = {
    val requestBuilder = ChatPostMessageRequest
      .builder()
      .text(message.message)
      .attachments(message.attachmentsList.asJava)

    val channelName = destination match {
      case SlackChannel(channelName) =>
        // change sender name for channel only
        requestBuilder.username(configManager.senderName.orNull)

        Right(channelName)
      case SlackUser(email) =>
        val userId = getUserByEmail(email).map(_.getId)

        userId.flatMap(startConversation) match {
          case Some(value) => Right(value)
          case None =>
            Left(userNotFound(email))
        }
      case _ => Left(unknownDestination)
    }

    channelName match {
      case Right(value) =>
        val request = requestBuilder
          .channel(value)
          .build()

        client.chatPostMessage(request).asScala
      case Left(error) => Future.failed(SendMessageError(error))
    }
  }
}
