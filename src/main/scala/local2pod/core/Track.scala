import local2pod.mypod._

package local2pod.core {

  case class Track(id: String,
                   title: String,
                   artist: String,
                   albumArtist: String,
                   album: String,
                   label: String,
                   year: Int,
                   trackNumber: Int,
                   discNumber: Int,
                   duration: Int,
                   path: String){

    var artworkLoaded = false

    val artworkId: String = {
      if(albumArtist != null && album != null){
        albumArtist + album
      }else if(album != null && artist != null){
        artist + album
      }else{
        id
      }
    }

    def getLibTrack: LibTrack = {
      val libTrack = new LibTrack()
      libTrack.set("id", id)
      libTrack.set("title", title)
      libTrack.set("artist", artist)
      libTrack.set("albumartist", albumArtist)
      libTrack.set("album", album)
      libTrack.set("composer", label)
      libTrack.set("year", year.toString)
      libTrack.set("songnum", trackNumber.toString)
      libTrack.set("cdnum", discNumber.toString)
      libTrack.set("time", (duration * 1000).toString)
      if(artworkLoaded){
        libTrack.set("has_artwork", "1")
        libTrack.set("artworkcnt", "1")
        libTrack.set("artworkId", artworkId)
      }
      libTrack.set("path", path)
      libTrack
    }
  }

}
