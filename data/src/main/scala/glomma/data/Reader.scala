package glomma.data

import scala.io.Source

/** Read lines of text from a file that is stored in JVM resource directory
  */
object Reader {
  def read(fileName: String): List[String] =
    Source.fromResource(fileName).getLines().toList
}
