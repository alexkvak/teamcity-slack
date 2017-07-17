package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{Branch, SRunningBuild}
import org.scalatest.{FlatSpec, Matchers}
import org.scalamock.scalatest.MockFactory

class MessageBuilderTest extends FlatSpec with MockFactory with Matchers {
  "MessageBuilder.compile" should "compile default template" in {
    val build = stub[SRunningBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getBuildId _ when() returns 2
    build.getBranch _ when() returns branch
    branch.getName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.NORMAL

    MessageBuilder(MessageBuilder.defaultMessage).compile(build) shouldEqual SlackAttachment(
      """
        |Full name - 2
        |Branch: default
        |Status: succeeded
      """.stripMargin.trim, Status.NORMAL.getHtmlColor)
  }
}
