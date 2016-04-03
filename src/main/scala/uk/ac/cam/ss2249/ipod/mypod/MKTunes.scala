import java.io._
import java.nio._;
import java.nio.channels._;

package uk.ac.cam.ss2249.ipod.mypod {
  class MKTunes(itunesDir: File, artworkDb: ArtworkDB, ipodName: String, guid: String) {
    val MPL_UID = 1234567890


    def writeDB(tracks: Array[LibTrack], playlists: Array[LibPlaylist]) = {

      val out: ByteBuffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
      out.order(ByteOrder.LITTLE_ENDIAN)

      iTunesDB.mkMhbd(out)
      var mhbdSize = out.position()
      val mhsdPos = out.position()

      iTunesDB.mkMhsd(out)
      var mhsdSize = out.position()

      iTunesDB.mkMhlt(out, tracks.length)
      for(track <- tracks){
        assembleMhit(out, track)
      }
      mhsdSize = out.position() - mhsdSize

      writeAllPlaylists(out, tracks, playlists, ipodName)
      mhbdSize = out.position - mhbdSize

      out.flip
      iTunesDB.mkMhbd(out, mhbdSize, 3)
      out.position(mhsdPos)
      iTunesDB.mkMhsd(out, mhsdSize, 1)


      Hash58.hashBuffer(out, guid)

      val outFilePath: File = new File(itunesDir, "iTunesDB")
      outFilePath.getParentFile().mkdirs()
      val channel: FileChannel = new FileOutputStream(outFilePath, false).getChannel();
      out.position(0)
      channel.write(out);
      channel.close();

    }

    def writeAllPlaylists(out: ByteBuffer, tracks: Array[LibTrack],
                          playlists: Array[LibPlaylist],
                          ipodName: String) = {
      val masterBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer(10 * 1024 * 1024)
      val allTrackIds = tracks.map(track => track.get("id"))
      val masterPlaylist = new LibPlaylist(ipodName, allTrackIds)
      createPlaylist(masterBuffer, masterPlaylist, true, MPL_UID)
      masterBuffer.flip

      val playlistsBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer(10 * 1024 * 1024)
      val playlistCount = playlists.length
      for(playlist <- playlists){
        val playlistId = getNextiTunesId
        createPlaylist(playlistsBuffer, playlist, false, playlistId)
      }
      playlistsBuffer.flip

      val podcastBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer(10000)
      iTunesDB.mkMhyp(podcastBuffer)
      podcastBuffer.flip

      iTunesDB.mkMhsd(out, 92 + playlistsBuffer.limit + masterBuffer.limit +
                        podcastBuffer.limit, 3)


      iTunesDB.mkMhlp(out, 2 + playlistCount)
      out.put(masterBuffer)
      out.put(playlistsBuffer)
      out.put(podcastBuffer)

      iTunesDB.mkMhsd(out, 92 + playlistsBuffer.limit + masterBuffer.limit, 2)
      iTunesDB.mkMhlp(out, 1 + playlistCount)

      masterBuffer.flip
      out.put(masterBuffer)
      playlistsBuffer.flip
      out.put(playlistsBuffer)
    }

    def createPlaylist(out: ByteBuffer, playlist: LibPlaylist, isHidden: Boolean,
                       playlistId: Int) = {
      val buffer: ByteBuffer = iTunesDB.Util.newByteBuffer(10 * 1024 * 1024)
      var childCount = 0
      var songsCount = 0
      for(id <- playlist.tracks){
        if(trackIdMap contains id){
          val itunesId = trackIdMap.get(id).get
          val currentId = getNextiTunesId;
          val mhodBuffer = iTunesDB.Util.newByteBuffer
          iTunesDB.mkMhod(mhodBuffer, itunesId)
          iTunesDB.mkMhip(buffer, 1, currentId, itunesId, mhodBuffer.position)
          childCount += 1
          songsCount += 1
          mhodBuffer.flip
          buffer.put(mhodBuffer)
        }
      }

      val playlistType = if (isHidden) 1 else 0
      iTunesDB.mkMhyp(out, buffer.position, playlist.name, playlistType, childCount,
                      playlistId, 0)

      buffer.flip
      out.put(buffer)

    }

    var lastUsediTunesId = 0
    def getNextiTunesId = {
      lastUsediTunesId += 1
      lastUsediTunesId
    }

    var lastUsedDbid = "0000000000000000"
    def getNextDbid = {
      lastUsedDbid = DBID.increment(lastUsedDbid)
      lastUsedDbid
    }

    var trackIdMap: Map[String, Int] = Map()
    def assembleMhit(out: ByteBuffer, track: LibTrack) = {
      val mhodChunks: ByteBuffer = ByteBuffer.allocateDirect(99999)
      mhodChunks.order(ByteOrder.LITTLE_ENDIAN)
      var mhodCount = 0

      for((key, value) <- track.map) {
        if(value != null && value.length > 0){
          if(iTunesDB.mkMhod(mhodChunks, key, value)){
            mhodCount += 1
          }
        }
      }

      val currentiTunesId = getNextiTunesId
      val currentDbid = getNextDbid

      trackIdMap += track.get("id") -> currentiTunesId


      iTunesDB.mkMhit(out, currentiTunesId, currentDbid, mhodChunks.position,
                      mhodCount, track, artworkDb)
      mhodChunks.flip
      out.put(mhodChunks)

    }
  }
}
