import local2pod.mypod._
import java.io._
import javax.imageio.ImageIO
import java.awt.image._
import com.mpatric.mp3agic._
import scala.util._
import com.typesafe.scalalogging._

package local2pod.core {

  object Track extends LazyLogging {
    def fromFile(t: (File, String), copyFileToiPod: Tuple2[File, String] => String) = t match{
      case(file, id) => {
        val mp3 = new Mp3File(file.getPath)
        val tags = mp3.getId3v2Tag
        val title = getTag(tags.getTitle _) match {
          case Some(t) => t
          case None => file.getName.replace(".mp3", "")
        }
        Track(id,
              title,
              getTag(tags.getArtist _),
              getTag(tags.getAlbumArtist _),
              getTag(tags.getAlbum _),
              getTag(tags.getComposer _),
              getTag(() => tags.getYear.toInt),
              getTag(() => tags.getTrack.toInt),
              getTag(() => tags.getPartOfSet.toInt),
              getTag(() => mp3.getLengthInSeconds.asInstanceOf[Int] - 1),
              copyFileToiPod(t),
              () => {
                getTag(tags.getAlbumImage) match {
                  case None => None
                  case Some(bytes) =>
                    Try(ImageIO.read(new ByteArrayInputStream(tags.getAlbumImage))) match {
                      case Success(i) => Some(i)
                      case Failure(e) => {
                        logger.error("Error reading artwork", e)
                        None
                      }
                    }
                }
              })
      }
    }

    def getTag[T](getter: () => T): Option[T] = {
      Try(getter()) match {
        case Success(null) => None
        case Success(v) => Some(v)
        case Failure(_) => None
      }
    }
  }

  case class Track(id: String,
                   title: String,
                   artist: Option[String],
                   albumArtist: Option[String],
                   album: Option[String],
                   label: Option[String],
                   year: Option[Int],
                   trackNumber: Option[Int],
                   discNumber: Option[Int],
                   durationSeconds: Option[Int],
                   path: String,
                   getAlbumArt: (() => Option[BufferedImage])){

    def duration = durationSeconds match {
      case Some(secs) => Some(secs * 1000)
      case None => None
    }

    var artworkLoaded = false

    def artworkId: String = {
      (albumArtist, album, artist) match {
        case (Some(a), Some(b), _) => a + b
        case (_, Some(b), Some(c)) => b + c
        case _ => id
      }
    }

    def getLibTrack: LibTrack = {
      val libTrack = new LibTrack()
      libTrack.set("id", Some(id))
      libTrack.set("title", Some(title))
      libTrack.set("artist", artist)
      libTrack.set("albumartist", albumArtist)
      libTrack.set("album", album)
      libTrack.set("composer", label)
      libTrack.set("year", year)
      libTrack.set("songnum", trackNumber)
      libTrack.set("cdnum", discNumber)
      libTrack.set("time", duration)
      if(artworkLoaded){
        libTrack.set("has_artwork", Some(1))
        libTrack.set("artworkcnt", Some(1))
        libTrack.set("artworkId", Some(artworkId))
      }
      libTrack.set("path", Some(path))
      libTrack
    }

  }

}
