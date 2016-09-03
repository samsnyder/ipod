package uk.ac.cam.ss2249.ipod

import io.airlift.airline.Command

@Command(name = "defrag", description = "Defragments the Artwork files")
class Defragment extends iPodCommand {

  override def run() = {
    super.run()

    load()

    ipod.defragArtwork

  }
}

