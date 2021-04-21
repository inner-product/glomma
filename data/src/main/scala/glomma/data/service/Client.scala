package glomma.data.client

import scala.concurrent.ExecutionContext.global

import cats.effect._
import org.http4s.client.blaze._

object Client extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    BlazeClientBuilder[IO](global).resource.use { client =>
      // use `client` here and return an `IO`.
      // the client will be acquired and shut down
      // automatically each time the `IO` is run.
      client.expect[String]("http://localhost:8080/hello/James")
      IO.unit.as(ExitCode.Success)
    }
  }
}
