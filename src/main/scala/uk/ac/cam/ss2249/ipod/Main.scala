import java.io._;
import org.apache.commons.cli._
import com.typesafe.scalalogging._
import io.airlift.airline._
import scala.collection.JavaConverters._

package uk.ac.cam.ss2249.ipod {
  object Main extends LazyLogging {

    def main(args: Array[String]) {
      System.setProperty("java.awt.headless", "true");

      val builder: Cli.CliBuilder[Runnable] = Cli.builder[Runnable]("ipod")
        .withDescription("the ipod installer")
        .withDefaultCommand(classOf[Help])
        .withCommands(classOf[Help], classOf[Status],
                      classOf[Install], classOf[Reset],
                      classOf[Clean], classOf[Defragment])


      builder.withGroup("settings")
        .withDescription("manage settings")
        .withDefaultCommand(classOf[SettingsLs])
        .withCommands(classOf[SettingsLs], classOf[SettingsSet]);



      // builder.withGroup("settings")
      //   .withDescription("manage settings")
      //   .withDefaultCommand(classOf[SettingsLs])
      //   .withCommands(classOf[SettingsLs], classOf[SettingsSet]);

      // builder.withGroup("playlists")
      //   .withDescription("manage playlists")
      //   .withDefaultCommand(classOf[PlaylistsLs])
      //   .withCommands(classOf[PlaylistsLs], classOf[PlaylistsAdd],
      //                 classOf[PlaylistsRm], classOf[PlaylistsClear]);




      builder.build.parse(args.toList.asJava).run

    }



    // def main(args: Array[String]) {

    //   val clOptions = CLOptions(args)
    //   if(clOptions.has("h")){
    //     clOptions.showHelp
    //   }else{
    //     val mountDir = new File(clOptions.get("i", "/Volumes/Sam Snyder’s iPod"))
    //     val ipod = iPodLibrary.loadMyLibrary(mountDir)
    //     logger.info("Found iPod at {}", ipod.mountDir)
    //     val ipodName = clOptions.get("n", "Sam Snyder's iPod")
    //     if(clOptions.has("reset")){
    //       ipod.reset
    //       iPodLibrary.loadMyLibrary(mountDir).writeToiPod(ipodName)
    //     }else{
    //       val libDir = new File(clOptions.get("l", "/Users/sam/Music/iPod Sync"))
    //       val localReader = new LocalReader(libDir, ipod)
    //       localReader.fill(ipodName)
    //     }
    //   }
    // }
  }
}
