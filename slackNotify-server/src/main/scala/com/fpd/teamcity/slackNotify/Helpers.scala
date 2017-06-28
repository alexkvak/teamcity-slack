package com.fpd.teamcity.slackNotify

import javax.servlet.http.HttpServletRequest

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
}
