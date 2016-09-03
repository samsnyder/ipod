package uk.ac.cam.ss2249.ipod.core

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, StandardCopyOption}

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import uk.ac.cam.ss2249.ipod.mypod.{ArtworkDB, MKTunes}

import scala.util.{Failure, Success, Try}

import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.shareNothing._

object iPodLibrary extends LazyLogging {

  def waitForDir(dir: File, doPrint: Boolean = true): Unit = {
    if(!dir.exists){
      if(doPrint){
        logger.info("Waiting for iPod at {}", dir)
      }
      logger.debug("Waiting for {}", dir)
      Thread sleep 500
      waitForDir(dir, doPrint = false)
    }
  }

  def loadMyLibrary(mountDir: File, guid: String): iPodLibrary = {
    waitForDir(new File(mountDir, "iPod_Control"))

    val lib = Try{
      val file = getMyLibraryFile(mountDir)
      logger.debug("Trying to load {}", mountDir)
      val bytes = Files.readAllBytes(file.toPath)
      bytes.unpickle[iPodLibrary]
    } match {
      case Success(library) => library
      case Failure(e) =>
        logger.error("Could not load library, creating new one.")
        logger.debug("Reason for new library", e)
        new iPodLibrary()
    }
    lib.mountDir = mountDir.getPath
    lib.guid = guid
    lib
  }

  def getMyLibraryFile(mountDir: File) = {
    new File(mountDir, "iPod_Control/MyLibrary.bin")
  }
}

class iPodLibrary private () extends LazyLogging {
  @transient var mountDir: String = ""
  @transient var guid: String = ""

  private var artworkDb = new ArtworkDB()
  var tracks: Map[String, Track] = Map()
  private var playlists: Array[Playlist] = Array()

  def reset = {
    logger.info("Resetting iPod at " + mountDir)
    val ic = new File(mountDir, "iPod_Control")
    FileUtils.deleteDirectory(new File(ic, "Artwork"))
    FileUtils.deleteDirectory(new File(ic, "Music"))
    Files.deleteIfExists(iPodLibrary.getMyLibraryFile(new File(mountDir)).toPath)
    Files.deleteIfExists(new File(ic, "iTunes/iTunesDB").toPath)
  }

  def writeToiPod(ipodName: String) = {
    val itunesDir = new File(mountDir, "iPod_Control/iTunes")
    val mkTunes = new MKTunes(itunesDir, artworkDb, ipodName, guid)
    val libTracks = tracks.map{
      case (_, track) => track.getLibTrack
    }.toArray
    val libPlaylists = playlists.map(_.getLibPlaylist)
    logger.debug("Writing " + libTracks.length + " tracks")
    mkTunes.writeDB(libTracks, libPlaylists)
    artworkDb.writeArtworkDb(new File(mountDir))
  }

  def clearPlaylists() = playlists = Array()

  def addPlaylist(playlist: Playlist) = {
    logger.debug("Adding playlist {}", playlist)
    playlists +:= playlist
  }

  def addTrack(track: Track) = {
    logger.debug("Adding {}", track.title)
    if(!artworkDb.hasArtId(track.artworkId)){
      logger.debug("Adding album art")
      track.getAlbumArt() match {
        case Some(image) =>
          logger.debug("Got image")
          artworkDb.addImage(image, track.artworkId, new File(mountDir))
          track.artworkLoaded = true
        case None => ()
      }
    }else{
      track.artworkLoaded = true
    }
    logger.debug("Got album art")
    tracks += track.id -> track
  }

  def copyFileToiPod(t: (File, String)): String = t match {
    case (file, id) =>
      val musicDir = new File(mountDir, "iPod_Control/Music")
      val targetName = "F" + (Math.abs(id.hashCode()) % 20)
      val fileName = Math.abs(id.hashCode) + ".mp3"
      val realPath = new File(musicDir, targetName + "/" + fileName)
      realPath.getParentFile.mkdirs()
      Files.copy(file.toPath, realPath.toPath, StandardCopyOption.REPLACE_EXISTING)
      ":iPod_Control:Music:" + targetName + ":" + fileName
  }

  def deleteTrack(id: String): Option[Track] = {
    val track = tracks get id
    track.map{
      track => {
        tracks -= track.id
        track.path map {
          path => {
            val relPath = path.stripPrefix(":").replace(":", "/")
            val absPath = new File(mountDir, relPath)
            logger.debug("Deleting file {}", absPath)
            Files.deleteIfExists(absPath.toPath)
          }
        }
      }
    }
    track
  }

  def defragArtwork = artworkDb.defrag(new File(mountDir))

  def hasId(id: String): Boolean = tracks contains id

  def getTrack(id: String): Option[Track] = tracks get id

  def numTracks = tracks.size

  def getIds: List[String] = tracks.map{
    case (id, _) => id
  }.toList

  def saveMyLibrary() = {
    val bytes = this.pickle.value
    val file = iPodLibrary.getMyLibraryFile(new File(mountDir))
    file.getParentFile.mkdirs()
    val os = new FileOutputStream(file)
    os.write(bytes)
    os.close()
  }

}
