package uk.ac.cam.ss2249.ipod.mypod

class LibTrack() {

  var map: Map[String, String] = Map("mediatype" -> "1")

  def set(key: String, value: Option[Any]) = value match {
    case Some(v) => map += key -> v.toString
    case None =>
  }

  def get(key: String) = {
    map.get(key) match {
      case Some(value) => if(value == null || value.length == 0) "0" else value
      case None => "0"
    }
  }

}
