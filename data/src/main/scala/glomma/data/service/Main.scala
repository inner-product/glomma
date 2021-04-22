package glomma.data.client

import cats.effect._
import glomma.data.data.Distributions

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Client(Distributions.small.sample(), Configuration()).run
}
