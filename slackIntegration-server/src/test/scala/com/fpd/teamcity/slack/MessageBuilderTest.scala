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
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: succeeded
      """.stripMargin.trim, MessageBuilder.statusNormalColor)
  }

  "MessageBuilder.compile" should "compile failure template" in {
    val build = stub[SRunningBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.FAILURE

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    MessageBuilder(MessageBuilder.defaultMessage).compile(build, viewResultsUrl) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: failed
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with unknown placeholders" in {
    val build = stub[SRunningBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE

    val messageTemplate = """{name}
                            |{unknown}
                          """.stripMargin

    MessageBuilder(messageTemplate).compile(build, "") shouldEqual SlackAttachment(
      s"""Full name
        |{unknown}
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }
}
