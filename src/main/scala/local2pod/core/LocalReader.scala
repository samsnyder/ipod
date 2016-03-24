import java.io._

package local2pod.core {

  class LocalReader(libraryDir: File) {
    val musicDir = new File(libraryDir, "music")
    val spotifyDir = new File(musicDir, "spotify")
    val localDir = new File(musicDir, "local")

    def getIds: List[(File, String)] = getSpotifyIds ++ getLocalIds

    def getSpotifyIds: List[(File, String)] = spotifyDir.listFiles.toList.
      map(f => (f, f.getName)).
      filter(_._2.contains(".mp3")).
      map{case (f, id) => (f, id.replace(".mp3", ""))}

    def getLocalIds = List()

    def getTags(file: File) = {
      
    }
  }

}
