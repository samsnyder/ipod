import java.io._;
// import java.awt.image._;
// import javax.imageio._;
// import local2pod.mypod._
import local2pod.core._
// import scala.pickling.Defaults._, scala.pickling.json._


package local2pod {
  object Main{

    case class Foo(a: List[Int], b: String, c: Long)

    def main(args: Array[String]) {
      import scala.pickling._
      // import json._

      import scala.pickling.Defaults._
      import scala.pickling.json._

      val f: Foo = Foo(List[Int](1, 2, 4), "ds", 4L)
      // val x = f.pickle


      // val lib = iPodLibrary.loadMyLibrary(new File("/Users/sam/Downloads/ipod5"))
      val i = 4
          val track = Track("00gm39uHwBnhjIvCkOU2SC" + i + System.currentTimeMillis,
                            "Test Song " + i,
                            "Test Artist" + (i),
                            "Test Alb Artist" + (i % 3),
                            "Test album" + (i % 6),
                            "Label" + (i % 2),
                            2000 + (i % 6),
                            i,
                            1,
                            321,
                            ":iPod_Control:Music:F5:00gm39uHwBnhjIvCkOU2SC.mp3")


      val v = new ArtworkDB(new File())
      // val v = Map[String, Int]("hi" -> 4)
      val p = track.pickle

      println(p)
      // val pckl = Array(1, 2, 3, 4).pickle
    }

    // def main2(args: Array[String]){

    //   val pkl = Array(1, 3, 3).pickle
    //   println(pkl)

    //   // for(i <- 1 to 15){
    //   //   val track = Track("00gm39uHwBnhjIvCkOU2SC" + i + System.currentTimeMillis,
    //   //                     "Test Song " + i,
    //   //                     "Test Artist" + (i),
    //   //                     "Test Alb Artist" + (i % 3),
    //   //                     "Test album" + (i % 6),
    //   //                     "Label" + (i % 2),
    //   //                     2000 + (i % 6),
    //   //                     i,
    //   //                     1,
    //   //                     321,
    //   //                     ":iPod_Control:Music:F5:00gm39uHwBnhjIvCkOU2SC.mp3")

    //   //   lib.addTrack(track, () => {
    //   //                  val in1: BufferedImage = ImageIO.read(new File("/Users/sam/Pictures/matteo.jpg"));
    //   //                  in1
    //   //                })

    //   // }

    //   // lib.saveMyLibrary
    //   // lib.writeToiPod

    //   val libDir = new File("/Users/sam/Downloads/spotlib1")
    //   val localReader = new LocalReader(libDir)

    //   val ids = localReader.getIds

    //   println(ids)
    // }
  }
}
