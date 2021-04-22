package glomma.data.client

import cats.effect._
import glomma.data.data.Distributions

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    // Attempt to make runs reproducible
    scala.util.Random.setSeed(12345654321L)

    val scenario =
      args match {
        case "small" :: _ =>
          println("Using small scenario")
          Distributions.small
        case "large" :: _ =>
          println("Using large scenario")
          Distributions.large
        case other :: _ =>
          sys.error(s"I don't know about the scenario ${other}. Please use small or large (small is the default)")
        case Nil =>
          println("Defaulting to small scenario")
          Distributions.small
      }
    Client(scenario.sample(), Configuration()).run
  }
}
