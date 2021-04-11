package glomma
package data

import cats.effect.IO

/**
 * Read lines of text from a file that is stored as a JVM resource
 */
object Reader {
  def read(fileName: String): IO[List[String]] =
    ???
}
