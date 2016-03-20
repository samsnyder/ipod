import java.nio._

package mypod {

  object iTunesDB {

    object Util {
      def writeAscii(out: ByteBuffer, s: String) = {
        for(c <- s){
          out.put(c.toByte)
        }
      }

      def writeUTF16(out: ByteBuffer, s: String) = {
        out.put(s.getBytes("UTF-16"), 0, s.length * 2)
      }

      def writeHexString(out: ByteBuffer, string: String) = {
        val s = if(string.length == 16) string else "0000000000000000"
        for(i <- 0 until s.length / 2){
          val byteHex = s(2*i).toString + s(2*i+1).toString
          writeByte(out, java.lang.Integer.parseInt(byteHex, 16))
        }
      }

      def writeLong(out: ByteBuffer, l: Long) = out.putLong(l)
      def writeShort(out: ByteBuffer, s: Int) = out.putShort(s.asInstanceOf[Short])
      def writeByte(out: ByteBuffer, b: Int) = out.put(b.asInstanceOf[Byte])

      def writeInt(out: ByteBuffer, n: Int) = out.putInt(n)

      def newByteBuffer = {
        val b: ByteBuffer = ByteBuffer.allocate(99999)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b
      }
    }

    def mkMhbd(out: ByteBuffer): Unit = mkMhbd(out, 0, 0)
    def mkMhbd(out: ByteBuffer, size: Int, childs: Int): Unit = {
      Util.writeAscii(out, "mhbd")
      Util.writeInt(out, 320)
      Util.writeInt(out, size + 320)
      Util.writeInt(out, 0x1)
      Util.writeInt(out, 0x19)
      Util.writeInt(out, childs)
      Util.writeInt(out, 0xE0ADECAD)
      Util.writeInt(out, 0x0DF0ADFB)
      Util.writeInt(out, 0x2)

      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)

      Util.writeInt(out, 0)

      for(i <- 1 to 67){
        Util.writeInt(out, 0)
      }
    }

    def mkMhsd(out: ByteBuffer): Unit = mkMhsd(out, 0, 0)
    def mkMhsd(out: ByteBuffer, size: Int, objType: Int): Unit = {
      Util.writeAscii(out, "mhsd")
      Util.writeInt(out, 96)
      Util.writeInt(out, size + 96)
      Util.writeInt(out, objType)
      for(i <- 1 to 20){
        Util.writeInt(out, 0)
      }
    }

    def mkMhlt(out: ByteBuffer, songNum: Int) = {
      Util.writeAscii(out, "mhlt")
      Util.writeInt(out, 92)
      Util.writeInt(out, songNum)
      for(i <- 1 to 20){
        Util.writeInt(out, 0)
      }
    }

    def mkMhod(out: ByteBuffer, key: String, value: String): Boolean =
      mkMhod(out, key, value, 1, false)
    def mkMhod(out: ByteBuffer, typeString: String, string: String,
               playlistId: Int, isPlaylist: Boolean) = {
      val objType: Int = if(isPlaylist){
        100
      }else{
        mhodIdMap.get(typeString) match{
          case Some(t) => t
          case None => -1
        }
      }

      if(objType < 0){
        false
      }else{
        val appendX: ByteBuffer = Util.newByteBuffer
        if(objType == 16 || objType == 15){
          Util.writeAscii(appendX, string)
        }else if(objType == 100){
          // Playlist
          Util.writeInt(appendX, playlistId)
          Util.writeInt(appendX, 0x00)
          for(_ <- 1 to 3){
            Util.writeInt(appendX, 0)
          }
        }else{
          // Normal mhod
          Util.writeInt(appendX, 1)
          Util.writeInt(appendX, string.length * 2)
          Util.writeInt(appendX, 0)
          Util.writeInt(appendX, 0)
          Util.writeUTF16(appendX, string)
        }

        Util.writeAscii(out, "mhod")
        Util.writeInt(out, 24)
        Util.writeInt(out, 24 + appendX.position)
        Util.writeInt(out, objType)
        Util.writeInt(out, 0)
        Util.writeInt(out, 0)
        appendX.flip
        out.put(appendX)

        true
      }

    }


    def mkMhit(out: ByteBuffer, size: Int, count: Int, track: LibTrack) = {
      println("SKIPPING VOLUME")
      println("SKIPPING RATING")

      val cId: Int = track.get("id").toInt
      val dbid: String = "ff60000000000000"
      val randCoverId: Int = 0

      println(size + " - " + count)

      Util.writeAscii(out, "mhit")
      Util.writeInt(out, 0x184)
      Util.writeInt(out, size + 0x184)
      Util.writeInt(out, count)
      Util.writeInt(out, cId)
      Util.writeInt(out, 1)
      Util.writeInt(out, 0)
      Util.writeShort(out, 0x100)
      Util.writeByte(out, track.get("compilation").toByte)
      Util.writeByte(out, track.get("rating").toByte)


      Util.writeInt(out, track.get("changetime").toInt)
      Util.writeInt(out, track.get("filesize").toInt)
      Util.writeInt(out, track.get("time").toInt)



      Util.writeInt(out, track.get("songnum").toInt)
      Util.writeInt(out, track.get("songs").toInt)
      Util.writeInt(out, track.get("year").toInt)
      Util.writeInt(out, track.get("bitrate").toInt)
      Util.writeShort(out, 0)
      Util.writeShort(out, track.get("srate").toInt)
      println("WEARID SRATE")
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("starttime").toInt)
      Util.writeInt(out, track.get("stoptime").toInt)
      Util.writeInt(out, track.get("soundcheck").toInt)
      Util.writeInt(out, track.get("playcount").toInt)
      Util.writeInt(out, track.get("playcount").toInt)
      Util.writeInt(out, track.get("lastplay").toInt)
      Util.writeInt(out, track.get("cdnum").toInt)
      Util.writeInt(out, track.get("cds").toInt)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("addtime").toLong.asInstanceOf[Int])
      Util.writeInt(out, track.get("bookmark").toInt)
      Util.writeHexString(out, dbid)
      Util.writeShort(out, 0)
      Util.writeShort(out, track.get("bpm").toInt)
      Util.writeShort(out, track.get("artworkcnt").toInt)
      Util.writeShort(out, 0)
      Util.writeInt(out, track.get("artworksize").toInt)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("releasedate").toInt)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("skipcount").toInt)
      Util.writeInt(out, track.get("lastskip").toInt)
      Util.writeByte(out, track.get("has_artwork").toInt)
      Util.writeByte(out, track.get("shuffleskip").toInt)
      Util.writeByte(out, track.get("bookmarkable").toInt)
      Util.writeByte(out, track.get("podcast").toInt)
      Util.writeHexString(out, track.get("dbid_2"))
      Util.writeByte(out, track.get("lyrics_flag").toInt)
      Util.writeByte(out, track.get("movie_flag").toInt)
      Util.writeByte(out, track.get("played_flag").toInt)
      Util.writeByte(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("pregap").toInt)
      Util.writeLong(out, track.get("samplecount").toLong)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("postgap").toInt)
      Util.writeInt(out, 0)
      Util.writeInt(out, track.get("mediatype").toInt)
      Util.writeInt(out, track.get("seasonnum").toInt)
      Util.writeInt(out, track.get("episodenum").toInt)
      for(_ <- 1 to 7) Util.writeInt(out, 0)
      Util.writeInt(out, track.get("gaplessdata").toInt)
      Util.writeInt(out, 0)
      Util.writeShort(out, track.get("has_gapless").toInt)
      Util.writeShort(out, track.get("nocrossfade").toInt)
      for(_ <- 1 to 23) Util.writeInt(out, 0)
      Util.writeInt(out, randCoverId)
      for(_ <- 1 to 8) Util.writeInt(out, 0)
    }



    val mhodIdMap: Map[String, Int] = Map("title" -> 1,
                                          "path" -> 2,
                                          "album" -> 3,
                                          "artist" -> 4,
                                          "genre" -> 5,
                                          "fdesc" -> 6,
                                          "eq" -> 7,
                                          "comment" -> 8,
                                          "category" -> 9,
                                          "composer" -> 12,
                                          "group" -> 13,
                                          "desc" -> 14,
                                          "podcastguid" -> 15,
                                          "podcastrss" -> 16,
                                          "chapterdata" -> 17,
                                          "subtitle" -> 18,
                                          "tvshow" -> 19,
                                          "tvepisode" -> 20,
                                          "tvnetwork" -> 21,
                                          "albumartist" -> 22,
                                          "artistthe" -> 23,
                                          "keywords" -> 24,
                                          "sorttitle" -> 27,
                                          "sortalbum" -> 28,
                                          "sortalbumartist" -> 29,
                                          "sortcomposer" -> 30,
                                          "sorttvshow" -> 31)
  }
}
