package uk.ac.cam.ss2249.ipod.core

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

import com.mpatric.mp3agic.Mp3File
import com.typesafe.scalalogging.LazyLogging
import uk.ac.cam.ss2249.ipod.mypod.LibTrack

import scala.util.{Failure, Success, Try}

object Track extends LazyLogging {
  def fromFile(t: (File, String), copyFileToiPod: Option[((File, String)) => String])
  = t match{
    case(file, id) =>
      val mp3 = new Mp3File(file.getPath)
      val tags = mp3.getId3v2Tag
      val title = getTag(tags.getTitle) match {
        case Some(value) => value
        case None => file.getName.replace(".mp3", "")
      }
      Track(id,
        title,
        getTag(tags.getArtist),
        getTag(tags.getAlbumArtist),
        getTag(tags.getAlbum),
        getTag(tags.getComposer),
        getTag(tags.getGenreDescription),
        getTag(() => tags.getYear.toInt),
        getTag(() => parseTrackNumber(tags.getTrack)),
        getTag(() => tags.getPartOfSet.toInt),
        getTag(() => mp3.getLengthInSeconds.asInstanceOf[Int] - 1),
        copyFileToiPod match {
          case Some(f) => Some(f(t))
          case None => None
        },
        () => {
          getTag(tags.getAlbumImage) match {
            case None => None
            case Some(bytes) =>
              Try(ImageIO.read(new ByteArrayInputStream(tags.getAlbumImage))) match {
                case Success(i) => Some(i)
                case Failure(e) =>
                  print(Console.RED)
                  logger.error("\tError reading artwork {}", e.getMessage)
                  logger.debug("Stack Trace", e)
                  print(Console.RESET)
                  None
              }
          }
        })
  }

  def getTag[T](getter: () => T): Option[T] = {
    Try(getter()) match {
      case Success(null) => None
      case Success(v) => Some(v)
      case Failure(_) => None
    }
  }

  def parseTrackNumber(str: String): Int = {
    Try(str.toInt) match {
      case Success(num) => num
      case Failure(_) => str.split("/")(0).toInt
    }
  }
}

case class Track(id: String,
                 title: String,
                 artist: Option[String],
                 albumArtist: Option[String],
                 album: Option[String],
                 label: Option[String],
                 genre: Option[String],
                 year: Option[Int],
                 trackNumber: Option[Int],
                 discNumber: Option[Int],
                 durationSeconds: Option[Int],
                 path: Option[String],
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
    libTrack.set("genre", genre)
    libTrack.set("year", year)
    libTrack.set("songnum", trackNumber)
    libTrack.set("cdnum", discNumber)
    libTrack.set("time", duration)
    if(artworkLoaded){
      libTrack.set("has_artwork", Some(1))
      libTrack.set("artworkcnt", Some(1))
      libTrack.set("artworkId", Some(artworkId))
    }
    libTrack.set("path", path)
    libTrack
  }

  override def toString = {
    val artistStr = artist match {
      case Some(a) => a
      case None => "Unknown Artist"
    }
    title + " - " + artistStr
  }

}
