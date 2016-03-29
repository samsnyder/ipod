import java.io._;
import java.nio._;
import java.nio.channels._;
import java.awt.image._;
import java.awt.{Graphics};
// import javax.imageio._;
import java.math.BigInteger;
import scala.collection.mutable.{ArrayBuffer};
import org.imgscalr._

package local2pod.mypod {
  case class ImageProfile(width: Int, height: Int, storageId: Int, drop: Int){
    val imageSize = width * height * 2 - drop
  }

  case class LoadedImage(val dbid: String, val sourceSize: Int, val id: Int,
                    val rating: Int, val subImages: Array[SubImage]) {
  }
  case class SubImage(val profile: ImageProfile, val offset: Int,
                      val path: String) {
    val vPadding = 0
    val hPadding = 0
  }

  // object ArtworkDB {
  //   def loadMyArt(artworkDir: String) = {
  //     try{
  //       val file = getMyArtFile(artworkDir)
  //       val ois = new ObjectInputStream(new FileInputStream(file))
  //       val artworkDb = ois.readObject().asInstanceOf[ArtworkDB]
  //       ois.close
  //       artworkDb
  //     }catch {
  //       case _: Throwable => {
  //         println("Could not load Artwork, creating new one")
  //         new ArtworkDB(artworkDir)
  //       }
  //     }
  //   }

  //   def getMyArtFile(artworkDir: String) = new File(artworkDir, "MyArtwork")

  // }

  object ArtworkDB {
    def getArtworkDir(mountDir: File) = new File(mountDir, "iPod_Control/Artwork")
  }

  class ArtworkDB() {
    private val MAX_ITHMB_SIZE: Long = 268435456L
    private var loadedImages: Map[String, LoadedImage] = Map()

    private val imageProfiles = Array[ImageProfile](
      ImageProfile(320, 320, 1060, 0),
      ImageProfile(128, 128, 1055, 0),
      ImageProfile(56, 56, 1061, 112)
    )

    def addImage(image: BufferedImage, artworkId: String, mountDir: File) = {
      val subImages = prepareImage(image)
      injectImage(subImages, artworkId, ArtworkDB.getArtworkDir(mountDir))
    }

    def hasArtId(artId: String): Boolean = loadedImages contains artId

    def getiTunesArtId(artId: String): Int = {
      loadedImages.get(artId) match {
        case Some(image) => image.id
        case None => 1
      }
    }

    // def saveMyArt = {
    //   val file = ArtworkDB.getMyArtFile(artworkDir)
    //   val oos = new ObjectOutputStream(new FileOutputStream(file))
    //   oos.writeObject(this)
    //   oos.close
    // }

    def writeArtworkDb(mountDir: File) = {
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
      iTunesDB.mkMhfd(out, mhfdSize, 0x03, getNextiTunesId)

      val artworkDbFile = new File(ArtworkDB.getArtworkDir(mountDir), "ArtworkDB")
      artworkDbFile.getParentFile().mkdirs()
      val channel: FileChannel = new FileOutputStream(artworkDbFile, false).getChannel();
      out.position(0)
      channel.write(out);
      channel.close();

    }

    private var lastiTunesIdUsed: Int = 100
    private var lastDbidUsed: String = "0000000000000000"

    private def getNextDbid = {
      lastDbidUsed = DBID.increment(lastDbidUsed)
      lastDbidUsed
    }

    private def getNextiTunesId = {
      lastiTunesIdUsed += 1
      lastiTunesIdUsed
    }

    private def injectImage(subImages: Array[BufferedImage], artworkId: String,
                            artworkDir: File) = {
      val imageDbid = getNextDbid
      val sourceSize = 0

      var subImageObjs: List[SubImage] = List()
      for(i <- 0 until subImages.length) {
        val subImage = subImages(i)
        val profile = imageProfiles(i)
        writeImageToiThmb(subImage, profile, artworkDir) match {
          case (fileName, imageOffset) => {
            subImageObjs :+= SubImage(profile, imageOffset, ":" + fileName)
          }
        }
      }

      val image = LoadedImage(imageDbid, sourceSize, getNextiTunesId,
                                  0, subImageObjs.toArray)

      loadedImages += artworkId -> image
    }

    private def writeImageToiThmb(subImage: BufferedImage, profile: ImageProfile,
                                  artworkDir: File) = {
      val fileName = findSmallEnoughiThmb(profile)
      val ithmbFile = new File(artworkDir, fileName)
      ithmbFile.getParentFile().mkdirs()

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

    private def findSmallEnoughiThmb(profile: ImageProfile): String =
      findSmallEnoughiThmb(profile, 1)
    private def findSmallEnoughiThmb(profile: ImageProfile, index: Int) = {
      "F" + profile.storageId + "_" + index + ".ithmb"
    }

    private def prepareImage(source: BufferedImage): Array[BufferedImage] = {
      imageProfiles.map(p => {
                          val image = Scalr.resize(source, p.width, p.height);
                          val resizedImage = new BufferedImage(p.width, p.height,
                                                               BufferedImage.TYPE_USHORT_565_RGB)
                          val g: Graphics = resizedImage.createGraphics()
                          g.drawImage(image, 0, 0, p.width, p.height, null)
                          g.dispose()
                          resizedImage
                        })
    }
  }

}
