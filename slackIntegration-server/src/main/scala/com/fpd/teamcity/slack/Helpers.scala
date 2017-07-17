package com.fpd.teamcity.slack

import javax.servlet.http.HttpServletRequest

import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.util.SessionUser

import scala.util.Random

object Helpers {
  implicit class RichHttpServletRequest(request: HttpServletRequest) {
    def param(key: String): Option[String] = Option(request.getParameter(key)).map(_.trim).filterNot(_.isEmpty)
  }

  implicit class RichRandom(random: Random) {
    def randomAlphaNumericString(length: Int): String = {
      val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
      randomStringFromCharList(length, chars)
    }

    private def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
      val sb = new StringBuilder
      for (_ ← 1 to length) {
        val randomNum = util.Random.nextInt(chars.length)
        sb.append(chars(randomNum))
      }
      sb.toString
    }
  }

  implicit class RichStatus(status: Status) {
    def isUnknown: Boolean = status.getPriority == Status.UNKNOWN.getPriority
  }

  def checkPermission(request: HttpServletRequest): Boolean =
    Option(SessionUser.getUser(request)).exists(user ⇒ user.isPermissionGrantedGlobally(Permission.CHANGE_SERVER_SETTINGS))
}
