import java.io._
import java.nio._;
import java.nio.channels._;

package mypod {
  object MKTunes {
    def writeDB() = {
      println("FSDf")

      val out: ByteBuffer = ByteBuffer.allocateDirect(99999);
      out.order(ByteOrder.LITTLE_ENDIAN)

      iTunesDB.mkMhbd(out)
      val mhbdSize = out.position()
      val mhbdPos = out.position()

      println(mhbdSize)

      iTunesDB.mkMhsd(out)
      val mhsdSize = out.position()

      iTunesDB.mkMhlt(out, 1)

      assembleMhit(out, new LibTrack())
      val mhsdSize = out.position() - mhsdSize

      println("Playlists")



      val outFilePath: File = new File("/Users/sam/Downloads/tunesdb.txt")
      val channel: FileChannel = new FileOutputStream(outFilePath, false).getChannel();
      out.flip();
      channel.write(out);
      channel.close();

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
