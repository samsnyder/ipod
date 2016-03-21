import java.io._
import java.nio._;
import java.nio.channels._;

package mypod {
  object MKTunes {
    val MPL_UID = 1234567890

    var sequence = 0

    def writeDB() = {
      println("FSDf")

      val out: ByteBuffer = ByteBuffer.allocateDirect(99999);
      out.order(ByteOrder.LITTLE_ENDIAN)

      iTunesDB.mkMhbd(out)
      val mhbdSize = out.position()
      val mhbdPos = out.position()

      println(mhbdSize)

      iTunesDB.mkMhsd(out)
      var mhsdSize = out.position()

      iTunesDB.mkMhlt(out, 1)

      assembleMhit(out, new LibTrack())
      mhsdSize = out.position() - mhsdSize

      println("Playlists")

      writeAllPlaylists(out)

      out.flip
      iTunesDB.mkMhbd(out, mhbdSize, 3)
      out.position(0)
      iTunesDB.mkMhsd(out, mhsdSize, 1)


      Hash58.hashBuffer(out, "000a27002135e037")

      val outFilePath: File = new File("/Users/sam/Downloads/tunesdb.txt")
      val channel: FileChannel = new FileOutputStream(outFilePath, false).getChannel();
      out.flip();
      channel.write(out);
      channel.close();

    }

    def writeAllPlaylists(out: ByteBuffer) = {
      val masterBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer
      val ipodName = "TESTIPOD"
      createPlaylist(masterBuffer, ipodName, List(1), true, MPL_UID)
      masterBuffer.flip

      val playlistsBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer
      val playlistId = 0x1234
      val playlistCount = 1
      createPlaylist(playlistsBuffer, "TestPLAY", List(1), false, playlistId)
      playlistsBuffer.flip

      val podcastBuffer: ByteBuffer = iTunesDB.Util.newByteBuffer(10000)
      iTunesDB.mkMhyp(podcastBuffer)
      podcastBuffer.flip

      iTunesDB.mkMhsd(out, 92 + playlistsBuffer.limit + masterBuffer.limit +
                        podcastBuffer.limit, 3)


      iTunesDB.mkMhlp(out, 2 + playlistCount)
      out.put(masterBuffer)
      out.put(playlistsBuffer)
      // iTunesDB.Util.writeAscii(out, "test")
      out.put(podcastBuffer)

      // iTunesDB.Util.writeAscii(out, "test")
      iTunesDB.mkMhsd(out, 92 + playlistsBuffer.limit + masterBuffer.limit, 2)
      iTunesDB.mkMhlp(out, 1 + playlistCount)

      masterBuffer.flip
      out.put(masterBuffer)
      playlistsBuffer.flip
      out.put(playlistsBuffer)
    }

    def createPlaylist(out: ByteBuffer, name: String, ids: List[Int],
                       isHidden: Boolean, playlistId: Int) = {
      val buffer: ByteBuffer = iTunesDB.Util.newByteBuffer
      var childCount = 0
      var songsCount = 0
      for(id <- ids){
        val currentId = getNextId;
        val mhodBuffer = iTunesDB.Util.newByteBuffer
        iTunesDB.mkMhod(mhodBuffer, id)
        iTunesDB.mkMhip(buffer, 1, playlistId, id, mhodBuffer.position)
        childCount += 1
        songsCount += 1
        mhodBuffer.flip
        buffer.put(mhodBuffer)
      }

      val playlistType = if (isHidden) 1 else 0
      iTunesDB.mkMhyp(out, buffer.position, name, playlistType, childCount,
                      playlistId, 0)

      buffer.flip
      out.put(buffer)

    }

    def getNextId = {
      sequence += 1
      sequence
    }

    def assembleMhit(out: ByteBuffer, track: LibTrack) = {
      val mhodChunks: ByteBuffer = ByteBuffer.allocateDirect(99999)
      mhodChunks.order(ByteOrder.LITTLE_ENDIAN)
      var mhodCount = 0

      for((key, value) <- track.map) {
        if(value.length > 0){
          if(iTunesDB.mkMhod(mhodChunks, key, value)){
            mhodCount += 1
          }
        }
      }

      iTunesDB.mkMhit(out, mhodChunks.position, mhodCount, track)
      mhodChunks.flip
      out.put(mhodChunks)

    }
  }
}
