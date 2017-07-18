package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{Branch, SRunningBuild}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class MessageBuilderTest extends FlatSpec with MockFactory with Matchers {
  "MessageBuilder.compile" should "compile default template" in {
    val build = stub[SRunningBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.NORMAL

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    MessageBuilder(MessageBuilder.defaultMessage).compile(build, viewResultsUrl) shouldEqual SlackAttachment(
      s"""
        |Full name - 2
        |Branch: default
        |Status: succeeded
        |<$viewResultsUrl|Open>
      """.stripMargin.trim, MessageBuilder.statusNormalColor)
  }
}
