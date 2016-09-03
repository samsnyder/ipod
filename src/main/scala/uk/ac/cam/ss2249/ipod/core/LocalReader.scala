package uk.ac.cam.ss2249.ipod.core

import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.mpatric.mp3agic.{ID3v1, Mp3File}
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.json4s._
import org.json4s.native.JsonMethods._

object LocalReader {
  def getMusicFiles(dir: File): List[File] = {
    dir.listFiles.toList.flatMap{
      file => {
        if(file.isDirectory){
          getMusicFiles(file)
        }else{
          List(file)
        }
      }
    }.filter(_.getName.contains(".mp3"))
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
      case string => URLEncoder.encode(string, StandardCharsets.UTF_8.toString()).replace("*", "%2A")
    }
  }

  def addPlaylists() = {
    ipodLib.clearPlaylists

    val file = new File(libraryDir, "playlists.json")
    val jsonString = scala.io.Source.fromFile(file).mkString
    parse(jsonString) match {
      case JObject(List((_, JArray(playlistObjs)))) =>
        playlistObjs.foreach{
          case JObject(List((_, JString(name)), (_, JArray(trackObjs)))) =>
            logger.debug("Adding {} with {} songs", name, trackObjs.length.toString)
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

  def save(ipodName: String) = {
    logger.debug("Adding playlists")
    addPlaylists()
    logger.debug("Writing to iPod")
    ipodLib.writeToiPod(ipodName)
    logger.debug("Saving library")
    ipodLib.saveMyLibrary()
  }
}
