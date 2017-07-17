package com.fpd.teamcity.slack

import java.io.{File, PrintWriter}

import jetbrains.buildServer.serverSide.ServerPaths
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.ext.EnumNameSerializer
import Helpers._

import scala.util.Random

class ConfigManager(paths: ServerPaths) {

  import ConfigManager._

  private implicit val formats = Serialization.formats(NoTypeHints) + new EnumNameSerializer(BuildSettingFlag)

  val configFile = new File(s"${paths.getConfigDir}/slackIntegration.json")

  private[teamcity] var config: Option[Config] = {
    if (configFile.exists()) {
      parse(configFile).extractOpt[Config]
    } else None
  }

  def oauthKey: Option[String] = config.map(_.oauthKey)

  def allBuildSettingList: BuildSettings = config.map(_.buildSettings).getOrElse(Map.empty)

  def buildSettingList(buildTypeId: String): BuildSettings = allBuildSettingList.filter(x ⇒ x._2.buildTypeId == buildTypeId)

  def buildSetting(id: String): Option[BuildSetting] = allBuildSettingList.get(id)

  private[teamcity] def update(config: Config): Unit = this.config = Some(config)

  def updateAndPersist(newConfig: Config): Boolean = synchronized {
    update(newConfig)
    val out = new PrintWriter(configFile, "UTF-8")
    try {
      writePretty(config, out)
    }
    finally {
      out.close()
    }

    true
  }

  @annotation.tailrec
  private def nextKey(map: BuildSettings): String = {
    val key = Random.randomAlphaNumericString(5)
    if (map.contains(key)) {
      nextKey(map)
    } else {
      key
    }
  }

  def updateBuildSetting(setting: BuildSetting, keyOption: Option[String]): Option[Boolean] = config.map { c ⇒
    val newSettings = keyOption match {
      case Some(key) ⇒
        c.buildSettings.updated(key, setting)
      case _ ⇒
        c.buildSettings + (nextKey(allBuildSettingList) → setting)
    }

    updateAndPersist(c.copy(buildSettings = newSettings))
  }

  def removeBuildSetting(key: String): Option[Boolean] = config.map { c ⇒
    updateAndPersist(c.copy(buildSettings = c.buildSettings - key))
  }

  def details: Map[String, Option[String]] = Map(
    "oauthKey" → oauthKey
  )
}

object ConfigManager {

  object BuildSettingFlag extends Enumeration {
    type BuildSettingFlag = Value

    val success, failureToSuccess, failure, successToFailure = Value
  }

  import BuildSettingFlag._

  type BuildSettings = Map[String, BuildSetting]

  case class BuildSetting(buildTypeId: String, branchMask: String, slackChannel: String, messageTemplate: String, flags: Set[BuildSettingFlag] = Set.empty) {
    // Getters for JSP
    def getBranchMask: String = branchMask
    def getSlackChannel: String = slackChannel
    def getMessageTemplate: String = messageTemplate
    // Flags
    def getSuccess: Boolean = flags.contains(BuildSettingFlag.success)
    def getFailureToSuccess: Boolean = flags.contains(BuildSettingFlag.failureToSuccess)
    def getFail: Boolean = flags.contains(BuildSettingFlag.failure)
    def getSuccessToFailure: Boolean = flags.contains(BuildSettingFlag.successToFailure)
  }

  case class Config(oauthKey: String, buildSettings: BuildSettings = Map.empty)

}
