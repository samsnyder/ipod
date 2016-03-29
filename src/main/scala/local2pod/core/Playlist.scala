import local2pod.mypod._

package local2pod.core {
  case class Playlist(name: String, tracks: Array[String]){
    def getLibPlaylist: LibPlaylist = {
      LibPlaylist(name, tracks.toArray)
    }
  }
}
