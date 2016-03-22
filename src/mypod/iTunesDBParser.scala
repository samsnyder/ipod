import java.nio._

package mypod {

  case class ParserObj(mapIm: Map[Any, Any]) {
    var map = mapIm
    def getInt(key: Any) = get(key).asInstanceOf[Int]
    def getString(key: Any) = get(key).asInstanceOf[String]
    def getObj(key: Any) = get(key).asInstanceOf[ParserObj]
    def get(key: Any) = if(has(key)) map.get(key).get else null
    def set(key: Any, value: Any) = {
      map = map + (key -> value)
    }
    def has(key: Any) = map contains key
  }

  object iTunesDBParse {

    object Util {
      def getAscii(in: ByteBuffer, o: Int, len: Int) = {
        in.position(o)
        val sb = new StringBuilder()
        for(_ <- 1 to len){
          sb.append(in.get().toChar)
        }
        sb.toString
      }

      def getUTF16(in: ByteBuffer, o: Int, len: Int) = {
        val sb = new StringBuilder()
        in.position(o)
        for(_ <- 1 to (len / 2)){
          sb.append(in.getChar())
        }
        sb.toString
      }

      def getInt(in: ByteBuffer, o: Int) = in.getInt(o)
      def getShort(in: ByteBuffer, o: Int) = in.getShort(o).toInt

      def getHexString(in: ByteBuffer, o: Int) = {
        val sb = new StringBuilder()
        in.position(o)
        for(_ <- 1 to 8) sb.append("%02X".format(in.get()))
        sb.toString
      }

      def skip(in: ByteBuffer, n: Int) = {
        for(_ <- 1 to n) in.get()
      }
    }

    def parseiTunesDB(in: ByteBuffer, myObj: ParserObj) = {
      var obj = myObj

      def parseiTunesDBRec(in: ByteBuffer, deep: Int): Unit = {
        val thisChilds = obj.getInt("childs")
        var foundType = ""

        in.position(obj.getInt("offset"))


        for(thisChild <- 1 to thisChilds){
          val t = Util.getAscii(in, obj.getInt("offset"), 4)
          // println("TYPE: " + t + " offset: " + obj.getInt("offset") + " depth: " + deep)

          val r = t match {
            case "mhfd" => getMhfd(in, obj.getInt("offset"))
            case "mhsd" => getMhsd(in, obj.getInt("offset"))
            case "mhii" => getMhii(in, obj.getInt("offset"))
            case "mhod" => getAwdbMhod(in, obj.getInt("offset"))
            case "mhni" => getMhni(in, obj.getInt("offset"))
            case "mhif" => getMhif(in, obj.getInt("offset"))
            case other => {
              if(other.matches("^mh(lt|lp|la|lf|li)$")){
                getMhxx(in, obj.getInt("offset"))
              }else{
                println("NO MATCH")
                ParserObj(Map())
              }
            }
          }

          val nextAtom = obj.getInt("offset") + r.getInt("header_size")
          if(!obj.has("tree")){
            obj.set("tree", ParserObj(Map()))
          }
          obj.getObj("tree").set(deep, ParserObj(Map("name" -> t, "ref" -> r)))

          if(nextAtom == obj.getInt("offset")) {
            println("BADPARSE 1")
          }else if(thisChild == 1) {
            foundType = t

            if(obj.getObj("tree").has(deep - 1)){
              val objName = obj.getObj("tree").getObj(deep - 1).get("name")
              if(obj.getObj("callback").has(objName) &&
                   obj.getObj("callback").getObj(objName).has("start")){

                val callback = obj.getObj("callback").getObj(objName)
                  .get("start").asInstanceOf[Any => Any]
                val args = ParserObj(Map(
                                       "offset" -> obj.getInt("offset"),
                                       "ref" -> obj.getObj("tree").getObj(deep-1).get("ref")
                                     ))
                callback(args)
              }
            }
          }else if(foundType != t){
            println("WALKING ERROR " + foundType + " - " + t)
          }

          if(obj.getObj("callback").has(t) && obj.getObj("callback").getObj(t).has("item")){
            val callback = obj.getObj("callback").getObj(t).get("item").asInstanceOf[Any => Any]
            // callback()
            val args = ParserObj(Map("offset" -> obj.getInt("offset"),
                                     "ref" -> r))
            callback(args)
          }

          if(obj.has("xhack") && t == "mhod" && thisChild == obj.getInt("xhack")){
            obj.set("xhack", null)
            foundType = "mhip"
          }

          obj.set("offset", obj.getInt("offset") + r.getInt("header_size"))
          obj.set("childs", r.getInt("childs"))
          if(obj.getInt("childs") > 0){
            parseiTunesDBRec(in, deep + 1)
          }

        }

        if(obj.getObj("tree").has(deep - 1)){
          val callbackName = obj.getObj("tree").getObj(deep-1).get("name")
          if(obj.getObj("callback").has(callbackName) &&
               obj.getObj("callback").getObj(callbackName).has("end")){
            val callback = obj.getObj("callback").getObj(callbackName)
              .get("end").asInstanceOf[Any => Any]
            val args = ParserObj(Map("offset" -> obj.getInt("offset"),
                                     "ref" -> obj.getObj("tree").getObj(deep-1).get("ref")))
            callback(args)
          }
        }
      }

      parseiTunesDBRec(in, 0)
    }

    def getMhfd(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
        "header_size" -> Util.getInt(in, o + 4),
        "total_size" -> Util.getInt(in, o + 8),
        "childs" -> Util.getInt(in, o + 20),
        "next_id" -> Util.getInt(in, o + 28)
      ))
    }

    def getMhsd(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 8),
                  "type" -> Util.getInt(in, o + 12),
                  "childs" -> 1
                ))
    }

    def getMhxx(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 4),
                  "childs" -> Util.getInt(in, o + 8)
                ))
    }

    def getMhii(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 8),
                  "childs" -> Util.getInt(in, o + 12),
                  "id" -> Util.getInt(in, o + 16),
                  "dbid" -> Util.getHexString(in, o + 20),
                  "rating" -> Util.getInt(in, o + 32),
                  "source_size" -> Util.getInt(in, o + 48)
                ))
    }

    def getMhni(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 8),
                  "childs" -> Util.getInt(in, o + 12),
                  "storage_id" -> Util.getInt(in, o + 16),
                  "offset" -> Util.getInt(in, o + 20),
                  "imgsize" -> Util.getInt(in, o + 24),
                  "vpadding" -> Util.getShort(in, o + 28),
                  "hpadding" -> Util.getShort(in, o + 30),
                  "height" -> Util.getShort(in, o + 32),
                  "width" -> Util.getShort(in, o + 34)
                ))
    }

    def getMhif(in: ByteBuffer, o: Int) = {
      ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 8),
                  "childs" -> Util.getInt(in, o + 12),
                  "id" -> Util.getInt(in, o + 16),
                  "imgsize" -> Util.getInt(in, o + 20)
                ))
    }

    def getAwdbMhod(in: ByteBuffer, o: Int) = {
      val obj = ParserObj(Map(
                  "header_size" -> Util.getInt(in, o + 4),
                  "total_size" -> Util.getInt(in, o + 8),
                  "type" -> Util.getInt(in, o + 12),
                  "type_string" -> mhodArray(Util.getInt(in, o + 12))
                  ))

      if(obj.getInt("type") == 2){
        obj.set("childs", 1)
      }else if(obj.getInt("type") == 3){
        val stringLength = Util.getInt(in, o + 24)
        obj.set("string",
                Util.getUTF16(in, o + obj.getInt("total_size") - stringLength, stringLength))
        println("HTRG " + obj.getString("string"))
        obj.set("header_size", obj.get("total_size"))
      }

      obj
    }

    val mhodArray: Array[String] = Array("title", "path", "album", "artist",
                                         "genre", "fdesc", "eq", "comment",
                                         "category", "", "", "composer",
                                         "group", "desc", "podcastguid",
                                         "podcastrss", "chapterdata",
                                         "subtitle", "tvshow", "tvepisode",
                                         "tvnetwork", "albumartist",
                                         "artistthe", "keywords", "", "",
                                         "sorttitle", "sortalbum",
                                         "sortalbumartist", "sortcomposer",
                                         "sorttvshow")


  }
}
