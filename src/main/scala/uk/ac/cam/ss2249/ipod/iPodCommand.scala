package uk.ac.cam.ss2249.ipod

import java.io.File

import ch.qos.logback.classic.{Level, Logger => JLogger}
import com.typesafe.scalalogging.LazyLogging
import io.airlift.airline.{Option, OptionType}
import org.slf4j.LoggerFactory
import uk.ac.cam.ss2249.ipod.core.{LocalReader, iPodLibrary}

abstract class iPodCommand extends LazyLogging with Runnable {

    @Option(`type` = OptionType.GLOBAL, name = Array("-d"),
            description = "Debug output")
    var debug: Boolean = _

    var ipod: iPodLibrary = _
    var localReader: LocalReader = _
    val ipodName = Settings.getiPodName
    val libDir = new File(Settings.getLibrary)
    val mountDir = new File(Settings.getiPod)
    val guid = Settings.getiPodGuid

    def load() = {
      ipod = iPodLibrary.loadMyLibrary(mountDir, guid)
      localReader = new LocalReader(libDir, ipod)
      logger.info("Found iPod at {}", ipod.mountDir)
    }

    override def run() = {
      if(debug){
        val root = LoggerFactory.getLogger("root").asInstanceOf[JLogger]
        root.setLevel(Level.DEBUG)
      }
    }

    def save() = {
      localReader.save(ipodName)
    }

  }

