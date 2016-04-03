import io.airlift.airline._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging._
import scala.util._
import scala.concurrent.{ Future, Promise }
import uk.ac.cam.ss2249.ipod.core._

package uk.ac.cam.ss2249.ipod {

  @Command(name = "status", description = "View the status of the iPod")
  class Status extends iPodCommand {
    val maxShow = 30

    override def run = {
      super.run

      load

      val localFiles = localReader.getIds
      val localIds = localFiles.map(_._2)
      val ipodIds = ipod.getIds

      val toInstall = localFiles.filter{
        case (_, id) => !ipod.hasId(id)
      }
      val toClean = ipod.getIds.filter(!localIds.contains(_))

      val numInstallShow = if(toInstall.size > maxShow) maxShow else toInstall.size
      val downShow = toInstall.slice(0, numInstallShow).map(Track.fromFile(_, None))
      val numCleanShow = if(toClean.size > maxShow) maxShow else toClean.size
      val cleanShow = toClean.slice(0, numCleanShow).map(ipod.getTrack(_)).flatten

      logger.info("iPod has {} tracks", ipod.numTracks.toString)
      logger.info("")

      logger.info("Need to install {} tracks.", toInstall.size.toString)
      print(Console.GREEN)
      downShow.foreach(logger.info("\t{}", _))
      print(Console.RESET)
      logger.info("")
      logger.info("Need to clean {} tracks.", toClean.size.toString)
      print(Console.RED)
      cleanShow.foreach(logger.info("\t{}", _))
      print(Console.RESET)


    }
  }

}
