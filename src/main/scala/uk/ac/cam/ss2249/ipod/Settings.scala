package uk.ac.cam.ss2249.ipod

import java.util.prefs.Preferences

import com.typesafe.scalalogging.LazyLogging
import io.airlift.airline.{Arguments, Command}

object Settings {
  private val prefs = Preferences.userRoot().node(this.getClass.getName)

  def getLibrary = prefs.get("library", ".")
  def getiPod = prefs.get("ipod", ".")
  def getiPodGuid = prefs.get("guid", "0")
  def getiPodName = prefs.get("name", "My iPod")

  def set(key: String, value: String) = prefs.put(key, value)
}

@Command(name = "set", description = "Set a settings")
class SettingsSet extends iPodCommand with LazyLogging {

  @Arguments(description = "Settings key and value",
    usage = "<key> <value>",
    required = true)
  var keyValue: java.util.List[String] = _

  override def run() = {
    super.run()

    if(keyValue != null && keyValue.size() == 2){
      val key = keyValue.get(0)
      val value = keyValue.get(1)
      Settings.set(key, value)

      logger.info("Set {} to \"{}\"", key, value)
    }else{
      logger.error("Please use ipod settings set <key> <value>")
    }
  }
}

@Command(name = "ls", description = "Show all settings")
class SettingsLs extends iPodCommand with LazyLogging {

  override def run() = {
    super.run()

    logger.info("The library path")
    logger.info("\tlibrary = \"{}\"\n", Settings.getLibrary)

    logger.info("The iPod mount directory")
    logger.info("\tipod = \"{}\"\n", Settings.getiPod)

    logger.info("The iPod GUID")
    logger.info("\tguid = \"{}\"\n", Settings.getiPodGuid)

    logger.info("The iPod Name")
    logger.info("\tname = \"{}\"\n", Settings.getiPodName)
  }

}


