package glomma.data.client

import org.http4s.Uri

final case class Configuration(
  host: String = "localhost",
  port: Int = 8808
) {
  def eventUrl: Uri = Uri.unsafeFromString(s"http://${host}:${port}/event")
  def bookUrl: Uri = Uri.unsafeFromString(s"http://${host}:${port}/books")
}
