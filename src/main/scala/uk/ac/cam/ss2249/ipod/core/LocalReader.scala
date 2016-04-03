import java.io._
import com.typesafe.scalalogging._
import org.json4s._
import org.json4s.native.JsonMethods._
import com.mpatric.mp3agic._
import java.net._

package uk.ac.cam.ss2249.ipod.core {

  object LocalReader {
    def getMusicFiles(dir: File): List[File] = {
      dir.listFiles.toList.map{
        file => {
          if(file.isDirectory){
            getMusicFiles(file)
          }else{
            List(file)
          }
        }
      }.flatten.filter(_.getName.contains(".mp3"))
    }
  }

  class LocalReader(libraryDir: File, ipodLib: iPodLibrary) extends LazyLogging {
    val musicDir = new File(libraryDir, "music")
    val spotifyDir = new File(musicDir, "spotify")
    val localDir = new File(musicDir, "local")

    def getIds: List[(File, String)] = getSpotifyIds ++ getLocalIds

    def getSpotifyIds: List[(File, String)] = {
      LocalReader.getMusicFiles(spotifyDir).map{
        file => (file, file.getName.replace(".mp3", ""))
      }
    }

    def getLocalIds: List[(File, String)] = {
      LocalReader.getMusicFiles(localDir).map{
        file => {
          val mp3 = new Mp3File(file.getPath)
          val tags: ID3v1 = if(mp3.getId3v2Tag == null){
            mp3.getId3v1Tag
          }else{
            mp3.getId3v2Tag
          }
          var id: String = null
          if(tags != null){
            id = spotEncode(tags.getArtist)
            id += ":" + spotEncode(tags.getAlbum)
            id += ":" + spotEncode(tags.getTitle)
          }
          if(id == "::" || id == null){
            id = "::" + spotEncode(file.getName.replace(".mp3", ""))
          }
          (file, id)
        }
      }
    }

    def spotEncode(s: String): String = {
      s match {
        case null => ""
        case s => URLEncoder.encode(s).replace("*", "%2A")
      }
    }

    def addPlaylists = {
      ipodLib.clearPlaylists

      val file = new File(libraryDir, "playlists.json")
      val jsonString = scala.io.Source.fromFile(file).mkString
      parse(jsonString) match {
        case JObject(List((_, JArray(playlistObjs)))) => {
          playlistObjs.map{
            case JObject(List((_, JString(name)), (_, JArray(trackObjs)))) => {
              logger.debug("Adding {}", name)
              val LocalRegex = "(.+):[0-9]{1,5}".r
              val tracks = trackObjs.map{
                case JString(t) => t
                case _ => ""
              }.map{
                case LocalRegex(rest) => rest
                case t => t
              }.toArray
              ipodLib.addPlaylist(Playlist(name, tracks))
            }
          }
        }
      }
    }

    // def fill(ipodName: String) = {

    //   val ids = getIds
    //   val toDownload = ids.filter{
    //     case (file, id) => !ipodLib.hasId(id)
    //   }
    //   logger.info("Adding {} songs.", toDownload.length.toString)

    //   var numDownload = 0
    //   val tracks = toDownload.grouped(20).foreach{
    //     group => {
    //       group.foreach{
    //         t => {
    //           val track = Track.fromFile(t, Some(t => ipodLib.copyFileToiPod(t)))
    //           ipodLib.addTrack(track)
    //           numDownload += 1
    //           logger.info("\rAdded {} - {}", track.title, track.artist)
    //           print(numDownload + " / " + toDownload.length)
    //         }
    //       }
    //       save(ipodName)
    //     }
    //   }
    //   save(ipodName)

    //   logger.info("Added all tracks")
    // }

    def save(ipodName: String) = {
      logger.debug("Adding playlists")
      addPlaylists
      logger.debug("Writing to iPod")
      ipodLib.writeToiPod(ipodName)
      logger.debug("Saving library")
      ipodLib.saveMyLibrary()
    }
  }

}
