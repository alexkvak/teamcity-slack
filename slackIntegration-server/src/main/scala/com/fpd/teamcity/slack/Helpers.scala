package com.fpd.teamcity.slack

import javax.servlet.http.HttpServletRequest

import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{SBuild, SBuildServer, SFinishedBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.util.SessionUser

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scala.util.Random

object Helpers {
  type Request = HttpServletRequest

  object Implicits {
    implicit class RichHttpServletRequest(val request: Request) extends AnyVal {
      def param(key: String): Option[String] = Option(request.getParameter(key)).map(_.trim).filterNot(_.isEmpty)
    }

    implicit def requestToUser(request: Request): Option[SUser] = Option(SessionUser.getUser(request))

    implicit class RichRandom(val random: Random) extends AnyVal {
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

    implicit class RichStatus(val status: Status) extends AnyVal {
      def isUnknown: Boolean = status.getPriority == Status.UNKNOWN.getPriority
    }

    implicit class RichBuild(val build: SBuild) extends AnyVal {
      def committees: Vector[String] = {
        val users = build.getContainingChanges.asScala.toVector.flatMap(_.getCommitters.asScala).distinct
        users.map(user ⇒ Option(user.getEmail).getOrElse("")).filter(_.length > 0)
      }

      def matchBranch(mask: String): Boolean =
        mask.r.findFirstIn(Option(build.getBranch).map(_.getDisplayName).getOrElse("")).isDefined
    }

    implicit class RichBuildServer(val sBuildServer: SBuildServer) extends AnyVal {
      def findPreviousStatus(build: SBuild): Status = {

        def filterEntry(x: SFinishedBuild): Boolean = if (build.getBranch == null)
          x.getBranch == null
        else
          Option(x.getBranch).exists(_.getDisplayName == build.getBranch.getDisplayName)

        val history = sBuildServer.getHistory.getEntriesBefore(build, false).asScala

        history.view
          .filter(filterEntry)         // branch name filter
          .filter(!_.isPersonal)       // ignore personal builds
          .map(_.getBuildStatus)
          .find(_ != Status.UNKNOWN) // ignore cancelled and aborted builds
          .getOrElse(Status.NORMAL)
      }
    }
  }
}
