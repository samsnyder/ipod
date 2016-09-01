import java.io._;
import org.apache.commons.cli._
import com.typesafe.scalalogging._
import io.airlift.airline._
import scala.collection.JavaConverters._
import scala.util._

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

      builder.build.parse(args.toList.asJava).run
    }

  }
}
