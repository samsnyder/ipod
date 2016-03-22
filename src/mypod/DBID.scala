
package mypod {
  object DBID{
    def increment(str: String) = {
      toString(fromString(str) + 1)
    }

    def greater(a: String, b: String) = {
      fromString(a) > fromString(b)
    }

    private def fromString(str: String) = {
      var n = BigInt(0)
      for(i <- 0 until 8){
        val part = Integer.parseInt(str.substring(2*i, 2*i+2), 16)
        n += BigInt(part) << 8*i
      }
      n
    }

    private def toString(num: BigInt) = {
      val sb = new StringBuilder()
      for(i <- 0 until 8){
        val b = (num >> (8*i)).toInt & 0xFF
        sb.append("%02X".format(b))
      }
      sb.toString
    }
  }
}
