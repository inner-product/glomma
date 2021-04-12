package glomma
package data

import scala.io.Source

import cats.effect.IO

/** Read lines of text from a file that is stored in JVM resource directory
  */
object Reader {
  def read(fileName: String): IO[List[String]] =
    IO.delay(Source.fromResource(fileName).getLines().toList)
}
