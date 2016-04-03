import io.airlift.airline._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging._
import scala.util._
import scala.concurrent.{ Future, Promise }
import uk.ac.cam.ss2249.ipod.core._

package uk.ac.cam.ss2249.ipod {

  @Command(name = "reset", description = "Resets the iPod")
  class Reset extends iPodCommand {

    override def run = {
      super.run

      load

      ipod.reset
      iPodLibrary.loadMyLibrary(mountDir, guid).writeToiPod(ipodName)
    }
  }

}
