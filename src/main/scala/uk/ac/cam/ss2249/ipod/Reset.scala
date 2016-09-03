package uk.ac.cam.ss2249.ipod

import io.airlift.airline.Command
import uk.ac.cam.ss2249.ipod.core.iPodLibrary

@Command(name = "reset", description = "Resets the iPod")
class Reset extends iPodCommand {

  override def run() = {
    super.run()

    load()

    ipod.reset
    iPodLibrary.loadMyLibrary(mountDir, guid).writeToiPod(ipodName)
  }
}

