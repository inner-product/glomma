package glomma.ingest

import cats.effect._
import cats.effect.std.Queue
import org.http4s.implicits._
import org.http4s.blaze.server._
import glomma.event.Event
import glomma.ingest.service.BookService
import scala.concurrent.ExecutionContext.global

object IngestServer extends IOApp {
  val bookService = new BookService()

  def run(args: List[String]): IO[ExitCode] =
    for {
      queue <- Queue.bounded[IO, Event](5)
      controller = new IngestController(queue, bookService)
      server = BlazeServerBuilder[IO](global)
        .bindHttp(8808, "localhost")
        .withHttpApp(controller.route.orNotFound)
        .resource
      exitCode <- server.use(_ => IO.never).as(ExitCode.Success)
    } yield exitCode
}
