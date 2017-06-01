package com.fpd.teamcity.slackNotify

import java.io.{File, PrintWriter}

import jetbrains.buildServer.serverSide.ServerPaths
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

case class Config(oauthKey: Option[String])

class ConfigManager(paths: ServerPaths) {
  private implicit val formats = Serialization.formats(NoTypeHints)

  val configFile = new File(s"${paths.getConfigDir}/slackNotify.json")

  private[teamcity] var config: Option[Config] = {
    if (configFile.exists()) {
      parse(configFile).extractOpt[Config]
    } else None
  }

  def oauthKey: Option[String] = config.flatMap(_.oauthKey)

  private[teamcity] def update(config: Config): Unit = {
    this.config = Some(config.copy(oauthKey = this.config.flatMap(_.oauthKey)))
  }

  def updateAndPersist(newConfig: Config): Unit = synchronized {
    update(newConfig)
    val out = new PrintWriter(configFile, "UTF-8")
    try {
      writePretty(config, out)
    }
    finally {
      out.close()
    }
  }

  def details: Map[String, Option[String]] = Map(
    "oauthKey" → oauthKey
  )
}
