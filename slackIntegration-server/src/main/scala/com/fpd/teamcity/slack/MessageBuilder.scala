package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.serverSide.SRunningBuild

class MessageBuilder(template: String) {
  def compile(build: SRunningBuild): SlackAttachment = {
    def status = if (build.getBuildStatus.isSuccessful) {
      "succeeded"
    } else {
      "failed"
    }

    // TODO: implement
    def mentions = ""

    val text = """\{(\w+)\}""".r.replaceAllIn(template, m ⇒ m.group(1) match {
      case "name" ⇒ build.getFullName
      case "number" ⇒ build.getBuildId.toString
      case "branch" ⇒ build.getBranch.getName
      case "status" ⇒ status
      case "mentions" ⇒ mentions
    })

    // TODO: add link to build page
    SlackAttachment(text.trim, build.getBuildStatus.getHtmlColor)
  }
}

object MessageBuilder {
  def defaultMessage: String =
    """{name} - {number}
      |Branch: {branch}
      |Status: {status}
      |{mentions}
    """.stripMargin

  def apply(template: String): MessageBuilder = new MessageBuilder(template)
}
