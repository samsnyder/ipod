
package mypod {
  class LibTrack() {
    val coverId = 1
    var map: Map[String, String] = Map("playcount" -> "0",
                                       "filesize" -> "4408873",
                                       "bitrate" -> "160",
                                       "title" -> "Till I\'m Gone - feat. Wiz Khalifa",
                                       "artist" -> "Tidffnie Tempah",
                                       "fdesc" -> "MP3 (MPEG audio layer 3)",
                                       "cdnum" -> "1",
                                       "mediatype" -> "1",
                                       "addtime" -> "3541171245",
                                       "year" -> "2011",
                                       "id" -> "testid",
                                       "soundcheck" -> "",
                                       "composer" -> "Parlophone UK",
                                       "srate" -> "44100",
                                       "volume" -> "0",
                                       "album" -> "Disc-Overy",
                                       "songnum" -> "16",
                                       "test" -> "",
                                       "path" -> ":iPod_Control:Music:F5:00gm39uHwBnhjIvCkOU2SC.mp3",
                                       "time" -> "213812")
    def get(key: String) = {
      map.get(key) match {
        case Some(value) => if(value.length == 0) "0" else value
        case None => "0"
      }
    }

  }
}
