import java.io._;
import local2pod.mypod._
import java.awt.image._;

package local2pod.core {
  object iPodLibrary {
    def loadMyLibrary(mountDir: File) = {
      try{
        val file = getMyLibraryFile(mountDir)
        val ois = new ObjectInputStream(new FileInputStream(file))
        val library = ois.readObject().asInstanceOf[iPodLibrary]
        ois.close
        library
      }catch {
        case _: Throwable => {
          println("Could not load library, creating new one")
          new iPodLibrary(mountDir)
        }
      }
    }

    def getMyLibraryFile(mountDir: File) = {
      new File(mountDir, "iPod_Control/MyLibrary")
    }
  }

  class iPodLibrary(mountDir: File) extends Serializable {
    val ipodControl = new File(mountDir, "iPod_Control")
    val itunesDir = new File(ipodControl, "iTunes")
    val artworkDir = new File(ipodControl, "Artwork")

    val artworkDb = new ArtworkDB(artworkDir)

    private var tracks: Map[String, Track] = Map()
    private val playlists: Map[String, Playlist] = Map()

    def writeToiPod = {
      val mkTunes = new MKTunes(itunesDir, artworkDb)
      val libTracks = tracks.map{
        case (_, track) => track.getLibTrack
      }.toArray.asInstanceOf[Array[LibTrack]]
      println("Got " + libTracks.length + " tracks")
      mkTunes.writeDB(libTracks, Array())
      artworkDb.writeArtworkDb
    }

    def addTrack(track: Track, getArtwork: () => BufferedImage) = {
      println("Adding " + track.title)
      if(!artworkDb.hasArtId(track.artworkId)){
        val image = getArtwork()
        if(image != null){
          artworkDb.addImage(image, track.artworkId)
          track.artworkLoaded = true
        }
      }else{
        track.artworkLoaded = true
      }
      tracks += track.id -> track
    }

    def getIds: Array[String] = tracks.map{
      case (id, _) => id
    }.toArray.asInstanceOf[Array[String]]


    def saveMyLibrary2 = {
      val file = iPodLibrary.getMyLibraryFile(mountDir)
      val oos = new ObjectOutputStream(new FileOutputStream(file))
      oos.writeObject(this)
      oos.close
    }

    def saveMyLibrary = {
      // val pkl = this.pickle
      // println(pkl)
    }

  }
}
