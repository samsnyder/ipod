import java.io._;
import java.nio._;
import java.nio.channels._;
import java.awt.image._;
import java.awt._;
import javax.imageio._;
import java.math.BigInteger;
import scala.collection.mutable.{ArrayBuffer};

package mypod {
  case class ImageProfile(width: Int, height: Int, storageId: Int, drop: Int)

  class LoadedImage(val dbid: String, val sourceSize: Int, val id: Int,
                    val rating: Int) {
    var subImages: Array[SubImage] = Array()
  }
  case class SubImage(val storageId: Int, val offset: Int, val imageSize: Int,
                      val vPadding: Int, val hPadding: Int, val height: Int,
                      val width: Int, val path: String){
  }



  class ArtworkDB(artworkDir: String) {
    val artworkDbFile = new File(artworkDir, "ArtworkDB")

    val imageProfiles = Array(
      ImageProfile(320, 320, 1060, 0),
      ImageProfile(128, 128, 1055, 0),
      ImageProfile(56, 56, 1061, 122)
    )

    def load = {
      object artworkObj {
        val offset = 0
        val childs = 1
      }
      val file: RandomAccessFile = new RandomAccessFile(artworkDbFile,"rw");
      val channel: FileChannel = file.getChannel();
      val fileSize = channel.size();
      val buffer: ByteBuffer = iTunesDB.Util.newByteBuffer(fileSize.asInstanceOf[Int]);
      channel.read(buffer);
      buffer.flip();


      val obj = ParserObj(Map(
        "offset" -> 0,
        "childs" -> 1,
        "awdb" -> 1,
        "callback" -> ParserObj(Map(
          "PACKAGE" -> 5,
          "mhod" -> ParserObj(Map("item" -> (mhodItem _))),
          "mhii" -> ParserObj(Map("start" -> (mhiiStart _))),
          "mhni" -> ParserObj(Map("start" -> (mhniStart _)))
        ))
      ))

      iTunesDBParse.parseiTunesDB(buffer, obj)
    }

    // Callbacks
    var mhniBuff: ParserObj = null
    def mhiiStart(args: ParserObj): Unit = {
      registerNewImage(args)
    }
    def mhniStart(args: ParserObj): Unit = {
      mhniBuff = args.getObj("ref")
    }
    def mhodItem(args: ParserObj): Unit = {
      if(args.getObj("ref").getInt("type") == 0x03){
        mhniBuff.set("path", args.getObj("ref").get("string"))
        registerSubImage(mhniBuff)
      }
    }


    var loadedImages: Map[String, LoadedImage] = Map()

    def writeArtworkDb = {
      val out: ByteBuffer = iTunesDB.Util.newByteBuffer;

      val mhfdFixup = out.position
      iTunesDB.mkMhfd(out)
      var mhfdSize = out.position

      val mhsdMhiiFixup = out.position
      iTunesDB.mkMhsd(out)
      var mhsdMhiiSize = out.position

      iTunesDB.mkMhxx(out, loadedImages.size, "mhli")

      loadedImages.foreach{
        case (id, image) => {
          val childBuffer = iTunesDB.Util.newByteBuffer
          for(subImage <- image.subImages){
            println("Writing subImage " + subImage)
            val subImageBuffer = iTunesDB.Util.newByteBuffer(2000)
            iTunesDB.mkAwdbMhod(subImageBuffer, 0x03, subImage.path)
            subImageBuffer.flip

            val payloadBuffer = iTunesDB.Util.newByteBuffer(2000)
            iTunesDB.mkMhni(payloadBuffer, subImage, 1, subImageBuffer)
            payloadBuffer.flip

            // iTunesDB.Util.writeAscii(childBuffer, "test")
            println("SDASDA " + subImageBuffer.limit)
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

      iTunesDB.mkMhxx(out, storageChunks.size, "mhlf")
      storageChunks.foreach{
        case (id, ithmbEntry) => {
          iTunesDB.mkMhif(out, 0, id, ithmbEntry.imageSize)
        }
      }
      println(mhfdSize + " - " + out.position)

      mhsdMhifSize = out.position - mhsdMhifSize
      mhfdSize = out.position - mhfdSize
      out.flip

      out.position(mhsdMhifFixup)
      iTunesDB.mkMhsd(out, mhsdMhifSize, 0x03)
      out.position(mhsdMhiiFixup)
      iTunesDB.mkMhsd(out, mhsdMhiiSize, 0x01)
      out.position(0)
      iTunesDB.mkMhfd(out, mhfdSize, 0x03, lastIdSeen + 1)

      val outFilePath: File = new File("/Users/sam/Downloads/ArtworkDB")
      val channel: FileChannel = new FileOutputStream(outFilePath, false).getChannel();
      out.position(0)
      channel.write(out);
      channel.close();

    }



    // class ImageInDB(dbid: String, s: Int, id: Int) {
    //   var subImages = Array[ParserObj]()
    //   var seen = false
    //   var size = s

    //   def addSubImage(sub: ParserObj) = subImages = subImages :+ sub
    //   def setSeen = seen = true

    // }

    // var context: String = ""
    // var imageMap: Map[String, ImageInDB] = Map()
    var lastIdSeen: Int = 0
    var lastDbidSeen: String = "0000000000000000"
    var currentDbid: String = ""

    def registerNewImage(args: ParserObj) = {
      val ref = args.getObj("ref")

      if(loadedImages contains ref.getString("dbid")){
        println("Image repeated")
      }else{
        if(!ref.has("id")){
          ref.set("id", lastIdSeen + 1)
        }
        val image = new LoadedImage(ref.getString("dbid"), ref.getInt("source_size"),
                                    ref.getInt("id"), 0)

        // val image = new ImageInDB(ref.getString("dbid"), ref.getInt("source_size"),
                              // ref.getInt("id"))
        loadedImages += (ref.getString("dbid") -> image)
        currentDbid = ref.getString("dbid")
        if(ref.getInt("id") > lastIdSeen){
          lastIdSeen = ref.getInt("id")
        }
        registerDbid(ref.getString("dbid"))
        println("New image registered " + ref.getString("dbid"))
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

    def registerSubImage(args: ParserObj) = {
      if(loadedImages contains currentDbid){
        val subImage = SubImage(args.getInt("storage_id"), args.getInt("offset"),
                                args.getInt("imgsize"), args.getInt("vpadding"),
                                args.getInt("hpadding"), args.getInt("height"),
                                args.getInt("width"), args.getString("path"))
        loadedImages.get(currentDbid).get.subImages :+= subImage
        registerStorage(subImage)
      }
    }

    var storageChunks: Map[Int, iThmbEntry] = Map()
    def registerStorage(subImage: SubImage) = {
      if(!storageChunks.contains(subImage.storageId)){
        val ithmbEntry: iThmbEntry = new iThmbEntry(subImage.imageSize)
        storageChunks += subImage.storageId -> ithmbEntry
      }
      storageChunks.get(subImage.storageId).get.used(subImage.path, subImage.offset)
    }

    class iThmbEntry(val imageSize: Int){
      private var entries: ParserObj = ParserObj(Map())
      def used(path: String, offset: Int) = {
        if(!entries.has(path)){
          entries.set(path, ParserObj(Map()))
        }
        if(!entries.getObj(path).has(offset)){
          entries.getObj(path).set(offset, 0)
        }
        entries.getObj(path).set(offset, entries.getObj(path).getInt(offset) + 1)
      }
    }



    // var storages: ParserObj = ParserObj(Map())
    // class iThmbLog(){
    //   private var entries: ParserObj = ParserObj(Map())
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

    // def registerStorage(storageId: Int, imageSize: Int, path: String, offset: Int) = {
    //   if(!storages.has(storageId)){
    //     storages.set(storageId, ParserObj(Map("ithmb" -> new iThmbLog(),
    //                                             "imgsize" -> imageSize)))
    //   }
    //   val ithumbLog: iThmbLog = storages.getObj(storageId).get("ithmb").asInstanceOf[iThmbLog]
    //   ithumbLog.used(path, offset)
    // }

    // def prepareImage(source: BufferedImage) = {
    //   var subImages = ArrayBuffer[BufferedImage]()

    //   for(p <- imageProfiles) {
    //     val resizedImage: BufferedImage = new BufferedImage(p.width, p.height,
    //                                                         BufferedImage.TYPE_USHORT_565_RGB)
    //     val g: Graphics = resizedImage.createGraphics()
    //     g.drawImage(source, 0, 0, p.width, p.height, null)
    //     g.dispose()

    //     subImages += resizedImage

    //     // ImageIO.write(resizedImage, "png", new File("/Users/sam/Downloads/" + p.width + ".png"));
    //   }

    //   subImages
    // }

    // def injectImage(subImages: ArrayBuffer[BufferedImage]) = {
    //   val imageId = getNextDbid
    //   registerNewImage(ParserObj(Map("ref" -> ParserObj(Map("id" -> 0, "dbid" -> imageId,
    //                                                         "source_size" -> 0)))))

    //   for(i <- 0 until imageProfiles.length){
    //     if(storages.has(imageProfiles(i).storageId)){
    //       println("Has Storgae")
    //       val dbInfo = storages.getObj(imageProfiles(i).storageId)

    //       val imageSize = (imageProfiles(i).width * imageProfiles(i).height * 2) -
    //         imageProfiles(i).drop

    //       registerSubImage(ParserObj(Map("storage_id" -> imageProfiles(i).storageId,
    //                                      "imgsize" -> imageSize,
    //                                      "path" -> (":" + dbInfo.getString("filename")),
    //                                      "offset" -> dbInfo.getInt("start"),
    //                                      "height" -> dbInfo.getInt("height"),
    //                                      "width" -> dbInfo.getInt("width"))))
    //     }
    //   }

    // }
    // def writeArtworkDb2 = {
    //   // WIPE LOST IMAGES

    //   val out: ByteBuffer = iTunesDB.Util.newByteBuffer;

    //   val mhfdFixup = out.position
    //   iTunesDB.mkMhfd(out)
    //   val mhfdSize = out.position

    //   val mhsdMhiiFixup = out.position
    //   iTunesDB.mkMhsd(out)
    //   val mhsdMhiiSize = out.position

    //   iTunesDB.mkMhxx(out, getImageIds.length, "mhli")

    //   for(imageId <- getImageIds) {
    //     val image = getImage(imageID)
    //     val mhiiChildPayload: ByteBuffer = iTunesDB.Util.newByteBuffer
    //     for(subImage <- image.subImages){
    //       val subImagePayload: ByteBuffer = iTunesDB.Util.newByteBuffer(2000)
    //       iTunesDB.mkAwdbMhod(subImagePayload, 0x03, subImage.getString("path"))
    //       subImagePayload.flip
    //       subImage.set("payload", subImagePayload)

    //       subImage.set("childs", 1)

    //     }
    //   }

    // }
  }

}
