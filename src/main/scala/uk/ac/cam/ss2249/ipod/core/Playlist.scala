package uk.ac.cam.ss2249.ipod.core

import uk.ac.cam.ss2249.ipod.mypod.LibPlaylist

case class Playlist(name: String, tracks: Array[String]){
  def getLibPlaylist: LibPlaylist = {
    LibPlaylist(name, tracks)
  }
}
