package com.fpd.teamcity.slackNotify

import java.io.{File, PrintWriter}

import jetbrains.buildServer.serverSide.ServerPaths
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

class ConfigManager(paths: ServerPaths) {

  import ConfigManager._

  private implicit val formats = Serialization.formats(NoTypeHints)

  val configFile = new File(s"${paths.getConfigDir}/slackNotify.json")

  private[teamcity] var config: Option[Config] = {
    if (configFile.exists()) {
      parse(configFile).extractOpt[Config]
    } else None
  }

  def oauthKey: Option[String] = config.map(_.oauthKey)

  def buildSettingList: BuildSettings = config.map(_.buildSettings).getOrElse(Map.empty)

  def buildSetting(id: String): Option[BuildSetting] = buildSettingList.get(id)

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

  def details: Map[String, Option[String]] = Map(
    "oauthKey" → oauthKey
  )
}

object ConfigManager {

  object BuildSettingFlag extends Enumeration {
    type BuildSettingFlag = Value

    val firstFail, lastFail = Value
  }

  import BuildSettingFlag._

  type BuildSettings = Map[String, BuildSetting]

  case class BuildSetting(branchMask: String, slackChannel: String, flags: Set[BuildSettingFlag] = Set.empty)

  case class Config(oauthKey: String, buildSettings: BuildSettings = Map.empty)

}
