import java.io._;
// import java.awt.image._;
// import javax.imageio._;
import local2pod.mypod._
import local2pod.core._
// import scala.pickling.Defaults._, scala.pickling.json._
import org.apache.commons.cli._
import com.typesafe.scalalogging._

package local2pod {
  object Main extends LazyLogging {

    case class CLOptions(args: Array[String]){
      lazy val options = {
        val options = new Options()
        options.addOption("h", "help", false, "show the usage")
        options.addOption("l", "library", true, "library directory")
        options.addOption("i", "ipod", true, "iPod mount directory")
        options.addOption("n", "name", true, "iPod name")
        options.addOption("reset", false, "reset the iPod")
      }

      lazy val cl = new GnuParser().parse(options, args)

      def showHelp = {
        val formatter = new HelpFormatter();
        formatter.printHelp("local2pod", options);
      }

      def has(flag: String) = cl.hasOption(flag)
      def get(flag: String) = cl.getOptionValue(flag)
      def get(flag: String, default: String) = cl.getOptionValue(flag, default)
    }

    def main(args: Array[String]) {
      System.setProperty("java.awt.headless", "true");

      val clOptions = CLOptions(args)
      if(clOptions.has("h")){
        clOptions.showHelp
      }else{
        val mountDir = new File(clOptions.get("i", "/Volumes/Sam Snyder’s iPod"))
        val ipod = iPodLibrary.loadMyLibrary(mountDir)
        logger.info("Found iPod at {}", ipod.mountDir)
        val ipodName = clOptions.get("n", "Sam Snyder's iPod")
        if(clOptions.has("reset")){
          ipod.reset
          iPodLibrary.loadMyLibrary(mountDir).writeToiPod(ipodName)
        }else{
          val libDir = new File(clOptions.get("l", "/Users/sam/Music/iPod Sync"))
          val localReader = new LocalReader(libDir, ipod)
          localReader.fill(ipodName)
        }
      }
    }
  }
}
