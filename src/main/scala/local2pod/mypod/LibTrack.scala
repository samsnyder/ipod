
package local2pod.mypod {
  class LibTrack() {

    var map: Map[String, String] = Map("mediatype" -> "1")

    // var map: Map[String, String] = Map("playcounta" -> "0",
    //                                    // "filesize" -> "4408873",
    //                                    // "bitrate" -> "160",
    //                                    "title" -> "Till I\'m Gone - feat. Wiz Khalifa",
    //                                    "artist" -> a,
    //                                    // "fdesc" -> "MP3 (MPEG audio layer 3)",
    //                                    "cdnum" -> "1",
    //                                    "mediatype" -> "1",
    //                                    // "addtime" -> "3541171245",
    //                                    "year" -> "2011",
    //                                    "id" -> a,
    //                                    // "soundcheck" -> "",
    //                                    "composer" -> "Parlophone UK",
    //                                    // "srate" -> "44100",
    //                                    // "volume" -> "0",
    //                                    "album" -> a,
    //                                    "songnum" -> "16",
    //                                    // "test" -> "",
    //                                    "has_artwork" -> "1",
    //                                    "artworkcnt" -> "1",
    //                                    "path" -> ":iPod_Control:Music:F5:00gm39uHwBnhjIvCkOU2SC.mp3",
    //                                    "time" -> "213812"
    //                                    )
    def set(key: String, value: String) = map += key -> value

    def get(key: String) = {
      map.get(key) match {
        case Some(value) => if(value.length == 0) "0" else value
        case None => "0"
      }
    }

  }
}
