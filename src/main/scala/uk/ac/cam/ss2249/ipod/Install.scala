import io.airlift.airline._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging._
import scala.util._
import scala.concurrent.{ Future, Promise }
import uk.ac.cam.ss2249.ipod.core._

package uk.ac.cam.ss2249.ipod {

  @Command(name = "install", description = "Install songs on the iPod")
  class Install extends iPodCommand {

    override def run = {
      super.run

      load

      val localIds = localReader.getIds
      val ipodIds = ipod.getIds

      val toDownload = localIds.filter{
        case (_, id) => !ipod.hasId(id)
      }

      logger.info("Adding {} songs.", toDownload.length.toString)

      var numDownload = 0
      val tracks = toDownload.grouped(20).foreach{
        group => {
          group.foreach{
            t => {
              val track = Track.fromFile(t, Some(t => ipod.copyFileToiPod(t)))
              ipod.addTrack(track)
              numDownload += 1

              print(Console.GREEN)
              logger.info("\r\t{} ({} / {})", track, numDownload.toString,
                          toDownload.length.toString)
              print(Console.RESET)
            }
          }
          save
        }
      }
      save

      logger.info("Added all tracks")

    }
  }

}
