package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{MessageSent, SlackChannel, SlackUser}
import com.ullink.slack.simpleslackapi.SlackMessageHandle
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.users.SUser
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future
import scala.util.Success

import NotificationSenderTest._

class NotificationSenderAsyncTest extends AsyncFlatSpec with AsyncMockFactory with Matchers {

  "NotificationSender.send" should "send message to channel for non-personal build" in {
    val sent = successfulSent

    val context = new Context {
      def settingFlags = Set(BuildSettingFlag.failure)

      build.getBuildStatus _ when() returns Status.FAILURE
      build.isPersonal _ when() returns false
      gateway.sendMessage _ when(SlackChannel(channelName), *) returns Future.successful(sent)

      val result = sender.send(build, Set(BuildSettingFlag.failure))
    }

    context.result.map(_.head shouldEqual sent)
  }

  "NotificationSender.send" should "send private message to build's owner for personal build" in {
    val sent = successfulSent

    val context = new Context {
      def settingFlags = Set(BuildSettingFlag.failure)

      build.getBuildStatus _ when() returns Status.FAILURE
      build.isPersonal _ when() returns true

      val email = "some@email.com"
      val user = stub[SUser]
      user.getEmail _ when() returns email
      build.getOwner _ when() returns user

      gateway.sendMessage _ when(SlackUser(email), *) returns Future.successful(sent)

      val result = sender.send(build, Set(BuildSettingFlag.failure))
    }

    context.result.map(_.head shouldEqual sent)
  }

  def successfulSent: MessageSent = Success(stub[SlackMessageHandle[SlackMessageReply]])
}
