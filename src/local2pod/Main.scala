import java.io._;
import java.awt.image._;
import java.awt._;
import javax.imageio._;
import local2pod.mypod._

package local2pod {
  object Main{
    def main(args: Array[String]){
      val aDb = new ArtworkDB("/Users/sam/Downloads/Artwork")


      aDb.loadFromFile


      val in1: BufferedImage = ImageIO.read(new File("/Users/sam/Pictures/matteo.jpg"));
      val artId1 = aDb.addImage(in1)

      // val file = new File("/Users/sam/Downloads/fhj.png")
      // ImageIO.write(images(1), "png", file);

      val tracks = Array(new LibTrack(artId1, "cool"), new LibTrack(artId1, "hi"),
                         new LibTrack("0600000000000000", "nice"))
      val playlists = Array(new LibPlaylist("playyyy", Array("cool", "hi")))

      val mkTunes = new MKTunes(aDb)
      mkTunes.writeDB(tracks, playlists)

      aDb.writeArtworkDb
    }
  }
}
