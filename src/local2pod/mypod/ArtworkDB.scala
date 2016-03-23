import java.io._;
import java.nio._;
import java.nio.channels._;
import java.awt.image._;
import java.awt.{Graphics};
import javax.imageio._;
import java.math.BigInteger;
import scala.collection.mutable.{ArrayBuffer};

package local2pod.mypod {
  case class ImageProfile(width: Int, height: Int, storageId: Int, drop: Int){
    val imageSize = width * height * 2 - drop
  }

  class LoadedImage(val dbid: String, val sourceSize: Int, val id: Int,
                    val rating: Int, val subImages: List[SubImage]) extends Serializable {
    // var subImages: Array[SubImage] = Array()
  }
  case class SubImage(val profile: ImageProfile, val offset: Int,
                      val path: String) extends Serializable {
    val vPadding = 0
    val hPadding = 0
  }

  object ArtworkDB {
    def loadMyArt(artworkDir: String) = {
      val file = new File(artworkDir, "mylib")
      val ois = new ObjectInputStream(new FileInputStream(file))
      val artworkDb = ois.readObject().asInstanceOf[ArtworkDB]
      ois.close
      artworkDb
    }
  }

  @SerialVersionUID(100L)
  class ArtworkDB(artworkDir: String) extends Serializable {
    val MAX_ITHMB_SIZE = 268435456

    var loadedImages: Map[String, LoadedImage] = Map()

    val artworkDbFile = new File(artworkDir, "ArtworkDB")

    val imageProfiles = Array(
      ImageProfile(320, 320, 1060, 0),
      ImageProfile(128, 128, 1055, 0),
      ImageProfile(56, 56, 1061, 112)
    )

    def addImage(image: BufferedImage, artworkId: String) = {
      val subImages = prepareImage(image)
      injectImage(subImages, artworkId)
    }

    def getiTunesArtId(artId: String): Int = {
      loadedImages.get(artId) match {
        case Some(image) => image.id
        case None => 0
      }
    }

    def saveMyArt = {
      println(loadedImages)
      val file = new File(artworkDir, "mylib")
      val oos = new ObjectOutputStream(new FileOutputStream(file))
      oos.writeObject(this)
      oos.close
    }

    // def loadFromFile = {
    //   try{
    //     val file: RandomAccessFile = new RandomAccessFile(artworkDbFile,"rw");
    //     val channel: FileChannel = file.getChannel();
    //     val fileSize = channel.size();
    //     val buffer: ByteBuffer = iTunesDB.Util.newByteBuffer(fileSize.asInstanceOf[Int]);
    //     channel.read(buffer);
    //     buffer.flip();


    //     val obj = ParserObj(Map(
    //                           "offset" -> 0,
    //                           "childs" -> 1,
    //                           "awdb" -> 1,
    //                           "callback" -> ParserObj(Map(
    //                                                     "PACKAGE" -> 5,
    //                                                     "mhod" -> ParserObj(Map("item" -> (mhodItem _))),
    //                                                     "mhii" -> ParserObj(Map("start" -> (mhiiStart _))),
    //                                                     "mhni" -> ParserObj(Map("start" -> (mhniStart _)))
    //                                                   ))
    //                         ))

    //     iTunesDBParse.parseiTunesDB(buffer, obj)
    //   }catch {
    //     case _: Throwable => {
    //       println("ArtorkDB not loaded")
    //     }
    //   }
    // }


    def writeArtworkDb = {
      val out: ByteBuffer = iTunesDB.Util.newByteBuffer(50 * 1024 * 2014);

      val mhfdFixup = out.position
      iTunesDB.mkMhfd(out)
      var mhfdSize = out.position

      val mhsdMhiiFixup = out.position
      iTunesDB.mkMhsd(out)
      var mhsdMhiiSize = out.position

      iTunesDB.mkMhxx(out, loadedImages.size, "mhli")

      loadedImages.foreach{
        case (_, image) => {
          val childBuffer = iTunesDB.Util.newByteBuffer
          for(subImage <- image.subImages){
            val subImageBuffer = iTunesDB.Util.newByteBuffer(2000)
            iTunesDB.mkAwdbMhod(subImageBuffer, 0x03, subImage.path)
            subImageBuffer.flip

            println("Writing subImage " + subImage)

            val payloadBuffer = iTunesDB.Util.newByteBuffer(2000)
            iTunesDB.mkMhni(payloadBuffer, subImage, 1, subImageBuffer)
            payloadBuffer.flip

            iTunesDB.mkAwdbMhod(childBuffer, 0x02, payloadBuffer)
          }

          childBuffer.flip
          iTunesDB.mkMhii(out, image.dbid, image.subImages.length, childBuffer,
                          image.id, image.rating, image.sourceSize)
        }
      }

      mhsdMhiiSize = out.position - mhsdMhiiSize

      val fakeMhla = iTunesDB.Util.newByteBuffer(1000)
      iTunesDB.mkMhxx(fakeMhla, 0, "mhla")
      fakeMhla.flip
      iTunesDB.mkMhsd(out, fakeMhla.limit, 0x02)
      out.put(fakeMhla)

      val mhsdMhifFixup = out.position
      iTunesDB.mkMhsd(out, 0, 0xff)
      var mhsdMhifSize = out.position

      iTunesDB.mkMhxx(out, imageProfiles.length, "mhlf")
      imageProfiles.foreach(
        profile => iTunesDB.mkMhif(out, 0, profile.storageId, profile.imageSize)
      )

      mhsdMhifSize = out.position - mhsdMhifSize
      mhfdSize = out.position - mhfdSize
      out.flip

      out.position(mhsdMhifFixup)
      iTunesDB.mkMhsd(out, mhsdMhifSize, 0x03)
      out.position(mhsdMhiiFixup)
      iTunesDB.mkMhsd(out, mhsdMhiiSize, 0x01)
      out.position(0)
      iTunesDB.mkMhfd(out, mhfdSize, 0x03, lastIdSeen + 1)

      val channel: FileChannel = new FileOutputStream(artworkDbFile, false).getChannel();
      out.position(0)
      channel.write(out);
      channel.close();

    }

    var lastIdSeen: Int = 100
    var lastDbidSeen: String = "0000000000000000"
    var currentDbid: String = ""

    def registerNewImage(image: LoadedImage) = {
      if(loadedImages contains image.dbid){
        println("Image repeated")
      }else{
        loadedImages += (image.dbid -> image)
        currentDbid = image.dbid
        if(image.id > lastIdSeen){
          lastIdSeen = image.id
        }
        registerDbid(image.dbid)
      }
    }

    def registerDbid(dbid: String) = {
      if(DBID.greater(dbid, lastDbidSeen)){
        lastDbidSeen = dbid
      }
      lastDbidSeen
    }

    def getNextDbid = {
      val dbid = DBID.increment(lastDbidSeen)
      registerDbid(dbid)
      dbid
    }

    // def registerSubImage(subImage: SubImage) = {
    //   if(loadedImages contains currentDbid){
    //     loadedImages.get(currentDbid).get.subImages :+= subImage
    //     registerStorage(subImage)
    //   }
    // }


    // // Callbacks
    // var mhniBuff: ParserObj = null
    // def mhiiStart(args: ParserObj): Unit = {
    //   val ref = args.getObj("ref")
    //   if(!ref.has("id")){
    //     ref.set("id", lastIdSeen + 1)
    //   }
    //   val image = new LoadedImage(ref.getString("dbid"), ref.getInt("source_size"),
    //                               ref.getInt("id"), 0)


    //   registerNewImage(image)
    // }
    // def mhniStart(args: ParserObj): Unit = {
    //   mhniBuff = args.getObj("ref")
    // }
    // def mhodItem(args: ParserObj): Unit = {
    //   if(args.getObj("ref").getInt("type") == 0x03){
    //     mhniBuff.set("path", args.getObj("ref").get("string"))
    //     val subImage = SubImage(mhniBuff.getInt("storage_id"), mhniBuff.getInt("offset"),
    //                             mhniBuff.getInt("imgsize"), mhniBuff.getInt("vpadding"),
    //                             mhniBuff.getInt("hpadding"), mhniBuff.getInt("height"),
    //                             mhniBuff.getInt("width"), mhniBuff.getString("path"))

    //     registerSubImage(subImage)
    //   }
    // }

    // case class StorageCacheEntry(start: Int, end: Int, fileName: String)

    def injectImage(subImages: Array[BufferedImage], artworkId: String) = {
      val imageDbid = getNextDbid
      val sourceSize = 0

      var subImageObjs: List[SubImage] = List()
      for(i <- 0 until subImages.length) {
        val subImage = subImages(i)
        val profile = imageProfiles(i)
        writeImageToiThmb(subImage, profile) match {
          case (fileName, imageOffset) => {
            subImageObjs :+= SubImage(profile, imageOffset, ":" + fileName)
          }
        }
      }

      val image = new LoadedImage(imageDbid, sourceSize, lastIdSeen + 1,
                                  0, subImageObjs)

      loadedImages += artworkId -> image
    }

    def writeImageToiThmb(subImage: BufferedImage, profile: ImageProfile) = {
      val fileName = findSmallEnoughiThmb(profile)
      val ithmbFile = new File(artworkDir, fileName)

      val shortBuffer = subImage.getData().getDataBuffer().asInstanceOf[DataBufferUShort]

      val byteBuffer = iTunesDB.Util.newByteBuffer(shortBuffer.getSize * 2)
      byteBuffer.asShortBuffer().put(shortBuffer.getData)
      byteBuffer.limit(profile.imageSize)

      val out = new FileOutputStream(ithmbFile, true).getChannel();
      val startOffset = out.position
      out.write(byteBuffer);
      val endOffset = out.position
      out.close();

      (fileName, startOffset.asInstanceOf[Int])
    }

    def findSmallEnoughiThmb(profile: ImageProfile): String =
      findSmallEnoughiThmb(profile, 1)
    def findSmallEnoughiThmb(profile: ImageProfile, index: Int) = {
      "F" + profile.storageId + "_" + index + ".ithmb"
    }

    // def injectImage2(subImages: Array[BufferedImage]) = {
    //   val imageDbid = getNextDbid
    //   val sourceSize = 0 // TODO: FIX
    //   val image = new LoadedImage(imageDbid, sourceSize, lastIdSeen + 1, 0)
    //   registerNewImage(image)
    //   for(i <- 0 until subImages.length) {
    //     val subImage = subImages(i)
    //     val profile = imageProfiles(i)
    //     val dbInfo = writeImageToDB(subImage, profile)
    //     // val imageSize = profile.width * profile.height * 2 - profile.drop

    //     val subImageObj = SubImage(profile.storageId, dbInfo.start, imageSize,
    //                                0, 0, subImage.getHeight, subImage.getWidth,
    //                                ":" + dbInfo.fileName)
    //     registerSubImage(subImageObj)
    //   }
    //   imageDbid
    // }

    // def writeImageToDB(subImage: BufferedImage, profile: ImageProfile) = {
    //   val filePrefix = "F" + profile.storageId + "_"
    //   val fileExt = ".ithmb"
    //   if(!storageChunks.contains(profile.storageId)){
    //     storageChunks += profile.storageId -> new iThmbEntry(0)
    //   }
    //   val ithmbEntry = storageChunks.get(profile.storageId).get
    //   var startOffset = ithmbEntry.lastOffsetUsed
    //   var fileIndex = ithmbEntry.lastIndexUsed

      // val dataBuffer = subImage.getData().getDataBuffer().asInstanceOf[DataBufferUShort]
    //   val dataLength = dataBuffer.getSize * 2 - profile.drop

    //   var fileName = ""
    //   var found = false
    //   while(!found){
    //     fileName = filePrefix + fileIndex + fileExt
    //     if(ithmbEntry.getUsed(":" + fileName, startOffset) == 0) {
    //       found = true
    //     }else if(startOffset >= MAX_ITHMB_SIZE) {
    //       startOffset = -dataLength
    //       fileIndex += 1
    //     }else{
    //       startOffset += dataLength
    //     }
    //   }

    //   ithmbEntry.lastIndexUsed = fileIndex
    //   ithmbEntry.lastOffsetUsed = startOffset

    //   val ithmbFile = new File(artworkDir, fileName)

    //   val byteBuffer = iTunesDB.Util.newByteBuffer(dataBuffer.getSize * 2)
    //   byteBuffer.asShortBuffer().put(dataBuffer.getData)
    //   byteBuffer.limit(dataLength)

    //   val out = new FileOutputStream(ithmbFile, true).getChannel();
    //   out.position(startOffset)
    //   out.write(byteBuffer);
    //   val endOffset = out.position
    //   out.close();

    //   StorageCacheEntry(startOffset, endOffset.toInt, fileName)
    // }

    // var storageChunks: Map[Int, iThmbEntry] = Map()
    // def registerStorage(subImage: SubImage) = {
    //   if(!storageChunks.contains(subImage.storageId)){
    //     val ithmbEntry: iThmbEntry = new iThmbEntry(subImage.imageSize)
    //     storageChunks += subImage.storageId -> ithmbEntry
    //   }
    //   storageChunks.get(subImage.storageId).get.used(subImage.path, subImage.offset)
    // }

    // class iThmbEntry(val imageSize: Int){
    //   private var entries: ParserObj = ParserObj(Map())

    //   var lastOffsetUsed = 0
    //   var lastIndexUsed = 1

    //   def getUsed(path: String, offset: Int) = {
    //     if(entries.has(path) && entries.getObj(path).has(offset)){
    //       entries.getObj(path).getInt(offset)
    //     }else{
    //       0
    //     }
    //   }

    //   def used(path: String, offset: Int) = {
    //     if(!entries.has(path)){
    //       entries.set(path, ParserObj(Map()))
    //     }
    //     if(!entries.getObj(path).has(offset)){
    //       entries.getObj(path).set(offset, 0)
    //     }
    //     entries.getObj(path).set(offset, entries.getObj(path).getInt(offset) + 1)
    //   }
    // }

    def prepareImage(source: BufferedImage) = {
      var subImages = Array[BufferedImage]()

      for(p <- imageProfiles) {
        val resizedImage: BufferedImage = new BufferedImage(p.width, p.height,
                                                            BufferedImage.TYPE_USHORT_565_RGB)
        val g: Graphics = resizedImage.createGraphics()
        g.drawImage(source, 0, 0, p.width, p.height, null)
        g.dispose()

        subImages = subImages :+ resizedImage

        // ImageIO.write(resizedImage, "png", new File("/Users/sam/Downloads/" + p.width + ".png"));
      }

      subImages
    }
  }

}
