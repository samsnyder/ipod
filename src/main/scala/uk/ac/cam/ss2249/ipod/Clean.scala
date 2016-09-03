package uk.ac.cam.ss2249.ipod

import io.airlift.airline.Command

@Command(name = "clean", description = "Removes songs on the iPod")
class Clean extends iPodCommand {

  override def run() = {
    super.run()

    load()

    val localIds = localReader.getIds.map(_._2)
    val ipodIds = ipod.getIds

    val toClean = ipodIds.filter(!localIds.contains(_))

    logger.info("Cleaning {} songs.", toClean.length.toString)

    var numCleaned = 0
    toClean.grouped(20).foreach{
      group => {
        group.foreach{
          id => {
            numCleaned += 1
            ipod.deleteTrack(id).foreach{
              track => {
                print(Console.RED)
                logger.info("\r\t{} ({} / {})", track, numCleaned.toString,
                  toClean.length.toString)
                print(Console.RESET)
              }
            }
          }
        }
        save()
      }
    }
    save()

    logger.info("Cleaned all tracks")
  }
}