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


      val in: BufferedImage = ImageIO.read(new File("/Users/sam/Pictures/matteo.jpg"));
      val images = aDb.prepareImage(in)
      aDb.injectImage(images)

      val file = new File("/Users/sam/Downloads/fhj.png")
      ImageIO.write(images(1), "png", file);

      val tracks = Array(new LibTrack(101, "cool"), new LibTrack(102, "hi"),
                         new LibTrack(103, "nice"))
      val playlists = Array(new LibPlaylist("playyyy", Array("cool", "hi")))

      val mkTunes = new MKTunes(aDb)
      mkTunes.writeDB(tracks, playlists)

      aDb.writeArtworkDb
    }
  }
}
