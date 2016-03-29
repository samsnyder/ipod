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


        // val ids = localReader.getIds
        // val toDownload = ids.filter{
        //   case (file, id) => !ipodLib.hasId(id)
        // }

        // var i = 0
        // val tracks = toDownload.grouped(20).foreach{
        //   group => {
        //     group.foreach{
        //       t => {
        //         val track = Track.fromFile(t, t => ipodLib.copyFileToiPod(t))
        //         ipodLib.addTrack(track)
        //         i += 1
        //         println(i)
        //       }
        //     }
        //     ipodLib.writeToiPod()
        //     ipodLib.saveMyLibrary()
        //   }
        // }

        // // val tracks = ids.map{
        // //   t => {
        // //     val track = Track.fromFile(t, t => ipodLib.copyFileToiPod(t))
        // //     ipodLib.addTrack(track)
        // //   }
        // // }

        // // println(tracks)

        // ipodLib.writeToiPod()
        // ipodLib.saveMyLibrary()

      }
    }

    // def main2(args: Array[String]){

    //   val pkl = Array(1, 3, 3).pickle
    //   println(pkl)

    //   // for(i <- 1 to 15){
    //   //   val track = Track("00gm39uHwBnhjIvCkOU2SC" + i + System.currentTimeMillis,
    //   //                     "Test Song " + i,
    //   //                     "Test Artist" + (i),
    //   //                     "Test Alb Artist" + (i % 3),
    //   //                     "Test album" + (i % 6),
    //   //                     "Label" + (i % 2),
    //   //                     2000 + (i % 6),
    //   //                     i,
    //   //                     1,
    //   //                     321,
    //   //                     ":iPod_Control:Music:F5:00gm39uHwBnhjIvCkOU2SC.mp3")

    //   //   lib.addTrack(track, () => {
    //   //                  val in1: BufferedImage = ImageIO.read(new File("/Users/sam/Pictures/matteo.jpg"));
    //   //                  in1
    //   //                })

    //   // }

    //   // lib.saveMyLibrary
    //   // lib.writeToiPod

    //   val libDir = new File("/Users/sam/Downloads/spotlib1")
    //   val localReader = new LocalReader(libDir)

    //   val ids = localReader.getIds

    //   println(ids)
    // }
  }
}
