package com.fpd.teamcity.slack

import jetbrains.buildServer.log.Loggers

class Logger {
  def log(message: String): Unit = Loggers.SERVER.info(s"${Strings.logCategory} - $message")
}
