package glomma.data.client

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.client.blaze._
import org.http4s.client._
import scala.concurrent.ExecutionContext.global

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
