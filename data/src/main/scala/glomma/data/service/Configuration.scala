package glomma.data.client

final case class Configuration(
  host: String = "localhost",
  port: Int = 8808
) {
  def url: String = s"http://${host}:${port}/event"
}
