import java.io._;
import uk.ac.cam.ss2249.ipod.mypod._
import uk.ac.cam.ss2249.ipod.core._
import com.typesafe.scalalogging._
import io.airlift.airline._
import ch.qos.logback.classic.{Logger => JLogger, _}
import org.slf4j._

package uk.ac.cam.ss2249.ipod {

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

    def load = {
      ipod = iPodLibrary.loadMyLibrary(mountDir, guid)
      localReader = new LocalReader(libDir, ipod)
      logger.info("Found iPod at {}", ipod.mountDir)
    }

    override def run = {

      if(debug){
        val root = LoggerFactory.getLogger("root").asInstanceOf[JLogger];
        root.setLevel(Level.DEBUG);
      }

      // val ipodName = clOptions.get("n", "Sam Snyder's iPod")
      // if(clOptions.has("reset")){
      //   ipod.reset
      //   iPodLibrary.loadMyLibrary(mountDir).writeToiPod(ipodName)
      // }else{
      //   val localReader = new LocalReader(libDir, ipod)
      //   localReader.fill(ipodName)
      // }
    }

    def save = {
      localReader.save(ipodName)
    }

  }

}
