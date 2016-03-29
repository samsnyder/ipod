import java.nio._

package local2pod.mypod {

  object iTunesDB {

    object Util {
      def writeAscii(out: ByteBuffer, s: String) = {
        for(c <- s){
          out.put(c.toByte)
        }
      }

      def writeUTF16(out: ByteBuffer, s: String) = {
        val bytes = s.getBytes("UTF-16")
        out.put(bytes, 3, s.length * 2 - 1)
        writeByte(out, 0)
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

      def newByteBuffer: ByteBuffer = newByteBuffer(9999)
      def newByteBuffer(size: Int) = {
        val b: ByteBuffer = ByteBuffer.allocate(size)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b
      }
    }


    def mkMhxx(out: ByteBuffer, childs: Int, name: String) = {
      Util.writeAscii(out, name)
      Util.writeInt(out, 0x5c)
      Util.writeInt(out, childs)
      for(_ <- 1 to 20) Util.writeInt(out, 0x00)
    }

    def mkMhfd(out: ByteBuffer): Unit = mkMhfd(out, 0, 0, 0)
    def mkMhfd(out: ByteBuffer, size: Int, childs: Int, nextId: Int) = {

      Util.writeAscii(out, "mhfd")
      Util.writeInt(out, 0x84)
      Util.writeInt(out, 0x84 + size)
      Util.writeInt(out, 0x00)
      Util.writeInt(out, 0x02)
      Util.writeInt(out, childs)
      Util.writeInt(out, 0x00)
      Util.writeInt(out, nextId)
      for(_ <- 1 to 4) Util.writeInt(out, 0)
      Util.writeInt(out, 0x02)
      for(_ <- 1 to 20) Util.writeInt(out, 0)
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

    def mkAwdbMhod(out: ByteBuffer, objType: Int, payloadAny: Any) {
      val append: ByteBuffer = Util.newByteBuffer(1000)
      if(objType == 0x03){
        val payload = payloadAny.asInstanceOf[String]
        Util.writeInt(append, payload.length * 2)
        Util.writeInt(append, 0x02)
        Util.writeInt(append, 0x00)
        Util.writeUTF16(append, payload)
      }else{
        val payload = payloadAny.asInstanceOf[ByteBuffer]
        append.put(payload)
      }

      val sizeHeader = 0x18
      val sizeMhod = sizeHeader + append.position

      Util.writeAscii(out, "mhod")
      Util.writeInt(out, sizeHeader)
      Util.writeInt(out, sizeMhod)
      Util.writeInt(out, objType)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      append.flip
      out.put(append)
    }

    def mkMhni(out: ByteBuffer, image: SubImage, childs: Int, payload: ByteBuffer) = {
      val sizeHeader = 0x4C
      val sizeMhni = sizeHeader + payload.limit

      Util.writeAscii(out, "mhni")
      Util.writeInt(out, sizeHeader)
      Util.writeInt(out, sizeMhni)
      Util.writeInt(out, childs)
      Util.writeInt(out, image.profile.storageId)
      Util.writeInt(out, image.offset)
      Util.writeInt(out, image.profile.imageSize)
      Util.writeShort(out, image.vPadding)
      Util.writeShort(out, image.hPadding)
      Util.writeShort(out, image.profile.height)
      Util.writeShort(out, image.profile.width)
      Util.writeInt(out, 0)
      Util.writeInt(out, image.profile.imageSize)
      for(_ <- 1 to 8) Util.writeInt(out, 0)
      out.put(payload)
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

    def mkMhlp(out: ByteBuffer, numPlaylists: Int) = {
      Util.writeAscii(out, "mhlp")
      Util.writeInt(out, 92)
      Util.writeInt(out, numPlaylists)
      for(_ <- 1 to 20) Util.writeInt(out, 0)
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
    def mkMhod(out: ByteBuffer, playlistId: Int): Boolean =
      mkMhod(out, null, null, playlistId, true)
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

    def mkMhif(out: ByteBuffer, childs: Int, id: Int, imageSize: Int) = {
      val sizeHeader = 0x7c
      val sizeMhif = sizeHeader

      Util.writeAscii(out, "mhif")
      Util.writeInt(out, sizeHeader)
      Util.writeInt(out, sizeMhif)
      Util.writeInt(out, childs)
      Util.writeInt(out, id)
      Util.writeInt(out, imageSize)
      for(_ <- 1 to 25) Util.writeInt(out, 0)
    }

    def mkMhii(out: ByteBuffer, dbid: String, childs: Int, payload: ByteBuffer,
               id: Int, rating: Int, sourceSize: Int) = {
      val sizeHeader = 0x98
      val sizeTotal = sizeHeader + payload.limit

      Util.writeAscii(out, "mhii")
      Util.writeInt(out, sizeHeader)
      Util.writeInt(out, sizeTotal)
      Util.writeInt(out, childs)
      Util.writeInt(out, id)
      Util.writeHexString(out, dbid)
      Util.writeInt(out, 0)
      Util.writeInt(out, rating)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeInt(out, sourceSize)
      Util.writeInt(out, 0)
      Util.writeInt(out, 1)
      Util.writeInt(out, 1)
      for(_ <- 1 to 22) Util.writeInt(out, 0)
      out.put(payload)
    }

    def mkMhip(out: ByteBuffer, childs: Int, playlistId: Int, trackId: Int, size: Int) = {
      Util.writeAscii(out, "mhip")
      Util.writeInt(out, 76)
      Util.writeInt(out, 76 + size)
      Util.writeInt(out, childs)
      Util.writeInt(out, 0)
      Util.writeInt(out, playlistId)
      Util.writeInt(out, trackId)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      for(_ <- 1 to 40) Util.writeByte(out, 0)
    }

    def mkMhyp(out: ByteBuffer, size: Int, name: String, playlistType: Int, fileNum: Int,
               playlistId: Int, mhodCount: Int): Unit  =
      mkMhyp(out, size, name, playlistType, fileNum, playlistId, mhodCount, false)

    def mkMhyp(out: ByteBuffer): Unit = mkMhyp(out, 0, "Podcasts", 0, 0, 0, 0, true)

    def mkMhyp(out: ByteBuffer, size: Int, name: String, playlistType: Int, fileNum: Int,
               playlistId: Int, mhodCount: Int, isPodcast: Boolean) = {
      val append: ByteBuffer = Util.newByteBuffer
      mkMhod(append, "title", name)

      writeDummyListView(append)

      Util.writeAscii(out, "mhyp")
      Util.writeInt(out, 108)
      Util.writeInt(out, size + 108 + append.position)
      Util.writeInt(out, 2 + mhodCount)
      Util.writeInt(out, fileNum)
      Util.writeInt(out, playlistType)
      Util.writeInt(out, 0)
      Util.writeInt(out, if(isPodcast) 0 else playlistId)
      Util.writeInt(out, 0)
      Util.writeInt(out, 0)
      Util.writeShort(out, 0) // spl
      val podcast = if(isPodcast) 1 else 0
      Util.writeByte(out, podcast)
      Util.writeByte(out, 0)
      Util.writeInt(out, 0)
      for(_ <- 1 to 60) Util.writeByte(out, 0)
      append.flip
      out.put(append)
    }

    def mkMhit(out: ByteBuffer, cId: Int, dbid: String, size: Int, count: Int,
               track: LibTrack, artworkDb: ArtworkDB) = {
      val volume = 50

      val coverId: Int = {
        if(track.get("has_artwork").toInt != 0){
          artworkDb.getiTunesArtId(track.get("artworkId"))
        }else{
          1
        }
      }

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
      Util.writeInt(out, volume)
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
      Util.writeByte(out, if(track.get("has_artwork").toInt == 0) 2 else 1)
      Util.writeByte(out, track.get("shuffleskip").toInt)
      Util.writeByte(out, track.get("bookmarkable").toInt)
      Util.writeByte(out, track.get("podcast").toInt)
      Util.writeHexString(out, track.get("dbid_2"))
      Util.writeByte(out, track.get("lyrics_flag").toInt)
      Util.writeByte(out, track.get("movie_flag").toInt)
      Util.writeByte(out, if(track.get("played_flag").toInt == 0) 2 else 1)
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
      Util.writeInt(out, coverId)
      for(_ <- 1 to 8) Util.writeInt(out, 0)
    }

    def writeDummyListView(out: ByteBuffer){
      Util.writeHexString(out, "6d686f6418000000")
      Util.writeHexString(out, "8802000064000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "8400010001000000")
      Util.writeHexString(out, "0900000000000000")
      Util.writeHexString(out, "0100250000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "0200c80001000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "0d003c0000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "04007d0000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "03007d0000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "0800640000000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "1700640001000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "1400500001000000")
      Util.writeHexString(out, "0000000000000000")
      Util.writeHexString(out, "15007d0001000000")
      for(_ <- 1 to 376) Util.writeByte(out, 0)
      Util.writeHexString(out, "6500000000000000")
      for(_ <- 1 to (76 - 4)) Util.writeByte(out, 0)
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
