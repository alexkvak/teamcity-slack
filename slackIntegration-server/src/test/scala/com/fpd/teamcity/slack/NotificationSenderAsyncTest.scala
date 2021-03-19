package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{SlackChannel, SlackUser}
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.users.SUser
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

import NotificationSenderTest._

class NotificationSenderAsyncTest
    extends AsyncFlatSpec
    with AsyncMockFactory
    with Matchers {

  "NotificationSender.send" should "send message to channel for non-personal build" in {

    var result: Future[Vector[Unit]] = Future.successful(Vector())

    new Context {
      def settingFlags = Set(BuildSettingFlag.failure)

      (build.getBuildStatus _).when().returns(Status.FAILURE)
      (build.isPersonal _).when().returns(false)
      gateway.sendMessage _ when (SlackChannel(
        channelName
      ), *) returns Future.unit

      result = sender.send(build, Set(BuildSettingFlag.failure))
    }

    result.map(_.size shouldEqual 1)
  }

  "NotificationSender.send" should "send private message to build's owner for personal build" in {
    var result: Future[Vector[Unit]] = Future.successful(Vector())

    new Context {
      def settingFlags = Set(BuildSettingFlag.failure)

      (build.getBuildStatus _).when().returns(Status.FAILURE)
      (build.isPersonal _).when().returns(true)

      val email = "some@email.com"
      private val user = stub[SUser]
      (user.getEmail _).when().returns(email)
      (build.getOwner _).when().returns(user)

      (gateway.sendMessage _)
        .when(SlackUser(email), *)
        .returns(
          Future.unit
        )

      result = sender.send(build, Set(BuildSettingFlag.failure))
    }

    result.map(_.size shouldEqual 1)
  }
}
