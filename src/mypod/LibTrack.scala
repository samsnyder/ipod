
package mypod {
  class LibTrack() {
    var map: Map[String, String] = Map("playcount" -> "0",
                                       "filesize" -> "4408873",
                                       "bitrate" -> "160",
                                       "title" -> "Till I\"m Gone - feat. Wiz Khalifa",
                                       "artist" -> "Tinie Tempah",
                                       "fdesc" -> "MP3 (MPEG audio layer 3)",
                                       "cdnum" -> "1",
                                       "mediatype" -> "1",
                                       "addtime" -> "3541171245",
                                       "year" -> "2011",
                                       "id" -> "1",
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
