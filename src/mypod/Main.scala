import java.io._;
import java.awt.image._;
import java.awt._;
import javax.imageio._;
import scala.collection.mutable.{ArrayBuffer};

package mypod {
  object Main{
    def main(args: Array[String]){
      val aDb = new ArtworkDB("/Users/sam/Downloads/Artwork")


      aDb.load


      // val in: BufferedImage = ImageIO.read(new File("/Users/sam/Downloads/artwork.jpg"));
      // val images: ArrayBuffer[BufferedImage] = aDb.prepareImage(in)
      // aDb.injectImage(images)

      val tracks = Array(new LibTrack())
      val playlists = Array(new LibPlaylist("playyyy", Array("testid", "cool")))

      val mkTunes = new MKTunes(aDb)
      mkTunes.writeDB(tracks, playlists)

      aDb.writeArtworkDb
    }
  }
}
