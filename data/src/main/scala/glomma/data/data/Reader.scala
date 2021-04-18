package glomma.data.data

import scala.io.{Codec, Source}

/** Read lines of text from a file that is stored in JVM resource directory
  */
object Reader {
  def read(fileName: String): List[String] =
    Source.fromResource(fileName)(Codec.UTF8).getLines().toList
}
