import java.io._;
import java.awt.image._;
import java.awt._;
import javax.imageio._;
import local2pod.mypod._

package local2pod {
  object Main{
    def main(args: Array[String]){
      val artworkDir = "/Users/sam/Downloads/Artwork"
      // val aDb = new ArtworkDB(artworkDir)

      val aDb = ArtworkDB.loadMyArt(artworkDir)


      // aDb.loadFromFile


      // val in1: BufferedImage = ImageIO.read(new File("/Users/sam/Pictures/matteo.jpg"));
      val in1: BufferedImage = ImageIO.read(new File("/Users/sam/Downloads/artwork.jpg"));
      aDb.addImage(in1, "art" + System.currentTimeMillis)

      // val file = new File("/Users/sam/Downloads/fhj.png")
      // ImageIO.write(images(1), "png", file);

      val tracks = Array(new LibTrack("art1", "cool"), new LibTrack("art1458748739053", "hi"),
                         new LibTrack("art1458748760233", "nice"))
      val playlists = Array(new LibPlaylist("playyyy", Array("cool", "hi")))

      val mkTunes = new MKTunes(aDb)
      mkTunes.writeDB(tracks, playlists)

      aDb.writeArtworkDb

      aDb.saveMyArt
    }
  }
}
