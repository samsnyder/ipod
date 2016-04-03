import uk.ac.cam.ss2249.ipod.mypod._

package uk.ac.cam.ss2249.ipod.core {
  case class Playlist(name: String, tracks: Array[String]){
    def getLibPlaylist: LibPlaylist = {
      LibPlaylist(name, tracks.toArray)
    }
  }
}
