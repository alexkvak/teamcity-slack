package com.fpd.teamcity.slack

import java.io.{File, PrintWriter}

import jetbrains.buildServer.serverSide.ServerPaths
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import org.json4s.ext.EnumNameSerializer
import Helpers.Implicits._

import scala.util.Random

class ConfigManager(paths: ServerPaths) {

  import ConfigManager._

  private implicit val formats: Formats = new DefaultFormats {
    override def alwaysEscapeUnicode: Boolean = true
  } + new EnumNameSerializer(BuildSettingFlag)

  private def configFile = new File(s"${paths.getConfigDir}/slackIntegration.json")

  private def backupFile = new File(s"${paths.getConfigDir}/slackIntegration.json.bak")

  private[teamcity] var config: Option[Config] = readConfig

  protected def readConfig: Option[Config] = if (configFile.exists()) {
    parse(configFile).extractOpt[Config]
  } else None

  def oauthKey: Option[String] = config.map(_.oauthKey)
  def publicUrl: Option[String] = config.flatMap(_.publicUrl)
  def senderName: Option[String] = config.flatMap(_.senderName).filter(_.nonEmpty)
  def enabled: Option[Boolean] = config.flatMap(_.enabled)
  def personalEnabled: Option[Boolean] = config.flatMap(_.personalEnabled)
  def sendAsAttachment: Option[Boolean] = config.flatMap(_.sendAsAttachment)

  def allBuildSettingList: BuildSettings = config.map(_.buildSettings).getOrElse(Map.empty)

  def buildSettingList(buildTypeId: String): BuildSettings = allBuildSettingList.filter {
    case (_, setting) ⇒ setting.buildTypeId == buildTypeId
  }

  def buildSetting(id: String): Option[BuildSetting] = allBuildSettingList.get(id)

  private def updateAndPersist(newConfig: Config): Boolean = {
    backup()
    this.config = Some(newConfig)
    persist()
  }

  protected def backup(): Boolean = writeConfig(backupFile)

  protected def persist(): Boolean = writeConfig(configFile)

  protected def writeConfig(file: File): Boolean = synchronized {
    val out = new PrintWriter(file, "UTF-8")
    try {
      writePretty(config, out)
    }
    finally {
      out.close()
    }

    true
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

  def update(authKey: String, pubUrl: String, personalEnabled: Boolean, enabled: Boolean, sender: String, sendAsAttachment: Boolean): Boolean = config match {
    case Some(c) ⇒
      updateAndPersist(c.copy(
        authKey, publicUrl = Some(pubUrl), personalEnabled = Some(personalEnabled),
        enabled = Some(enabled), senderName = Some(sender),
        sendAsAttachment = Some(sendAsAttachment)
      ))
    case None ⇒
      updateAndPersist(Config(
        authKey, publicUrl = Some(pubUrl), personalEnabled = Some(personalEnabled),
        enabled = Some(enabled), senderName = Some(sender),
        sendAsAttachment = Some(sendAsAttachment)
      ))
  }

  def removeBuildSetting(key: String): Option[Boolean] = config.map { c ⇒
    updateAndPersist(c.copy(buildSettings = c.buildSettings - key))
  }

  def details: Map[String, Option[String]] = Map(
    "oauthKey" → oauthKey,
    "publicUrl" → publicUrl,
    "senderName" → senderName,
    "enabled" → enabled.filter(x ⇒ x).map(_ ⇒ "1"),
    "personalEnabled" → personalEnabled.filter(x ⇒ x).map(_ ⇒ "1"),
    "sendAsAttachment" → sendAsAttachment.filter(x ⇒ x).map(_ ⇒ "1")
  )

  def isAvailable: Boolean = config.exists(c ⇒ c.enabled.exists(b ⇒ b) && c.oauthKey.length > 0)
}

object ConfigManager {

  object BuildSettingFlag extends Enumeration {
    type BuildSettingFlag = Value

    val success, failureToSuccess, failure, successToFailure, canceled, started, queued = Value
  }

  import BuildSettingFlag._

  type BuildSettings = Map[String, BuildSetting]

  case class BuildSetting(buildTypeId: String,
                          branchMask: String,
                          slackChannel: String,
                          messageTemplate: String,
                          flags: Set[BuildSettingFlag] = Set.empty,
                          artifactsMask: String = "",
                          deepLookup: Boolean = false,
                          notifyCommitter: Boolean = false,
                          maxVcsChanges: Int = BuildSetting.defaultMaxVCSChanges
                         ) {
    // Getters for JSP
    lazy val getBranchMask: String = branchMask
    lazy val getSlackChannel: String = slackChannel
    lazy val getMessageTemplate: String = messageTemplate
    lazy val getArtifactsMask: String = artifactsMask
    lazy val getDeepLookup: Boolean = deepLookup
    lazy val getNotifyCommitter: Boolean = notifyCommitter
    lazy val getMaxVcsChanges: Int = maxVcsChanges
    // Flags
    lazy val getSuccess: Boolean = flags.contains(BuildSettingFlag.success)
    lazy val getFailureToSuccess: Boolean = flags.contains(BuildSettingFlag.failureToSuccess)
    lazy val getFail: Boolean = flags.contains(BuildSettingFlag.failure)
    lazy val getSuccessToFailure: Boolean = flags.contains(BuildSettingFlag.successToFailure)
    lazy val getCanceled: Boolean = flags.contains(BuildSettingFlag.canceled)
    lazy val getStarted: Boolean = flags.contains(BuildSettingFlag.started)
    lazy val getQueued: Boolean = flags.contains(BuildSettingFlag.queued)

    /**
      * Removes success flag if failureToSuccess is set
      * and failure flag if successToFailure is set
      *
      * @return
      */
    lazy val pureFlags: Set[BuildSettingFlag] = {
      var pure = flags
      if (flags.contains(BuildSettingFlag.failureToSuccess)) {
        pure = pure - BuildSettingFlag.success
      }
      if (flags.contains(BuildSettingFlag.successToFailure)) {
        pure = pure - BuildSettingFlag.failure
      }
      pure
    }
  }

  object BuildSetting {
    lazy val defaultMaxVCSChanges = 5
  }

  case class Config(oauthKey: String,
                    buildSettings: BuildSettings = Map.empty,
                    publicUrl: Option[String] = None,
                    personalEnabled: Option[Boolean] = Some(true),
                    sendAsAttachment: Option[Boolean] = Some(true),
                    enabled: Option[Boolean] = Some(true),
                    senderName: Option[String] = None
                   )

  @annotation.tailrec
  private def nextKey(map: BuildSettings): String = {
    val key = Random.randomAlphaNumericString(5)
    if (map.contains(key)) {
      nextKey(map)
    } else {
      key
    }
  }
}
