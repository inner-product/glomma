package glomma.data.client

import scala.concurrent.ExecutionContext.global

import cats.effect._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._
import glomma.data.data._
import glomma.event.Event
import scala.collection.immutable.LazyList

final case class Client(scenario: Scenario, configuration: Configuration) {
  val badEvent = BadEvent(scenario)

  def events(): LazyList[Event] = {
    val badEvents: LazyList[Event] = LazyList.continually(badEvent.generate.sample())

    val goodEvents: LazyList[Event] =
      LazyList.unfold((List.empty[Event], scenario.sessions)){ case (events, sessions) =>
        events match {
          case Nil =>
            sessions match {
	            case Nil => None
              case h :: t =>
                val nextEvents = h.toEvents
                val evt = nextEvents.head
                val rest = nextEvents.tail
                Some((evt, (rest, t)))
            }
          case h :: t => Some((h, (t, sessions)))
        }
    }

    val evts = LazyList.unfold((badEvents, goodEvents)){ case (bad, good) =>
      if(scala.util.Random.nextDouble() < 0.2) Some((bad.head, (bad.tail, good)))
      else
        if(goodEvents.isEmpty) None
        else Some((good.head, (badEvents, good.tail)))
    }

    evts
  }

  def run(implicit ce: ConcurrentEffect[IO]): IO[ExitCode] = {
    BlazeClientBuilder[IO](global).resource.use{ client =>
      val evts = events()

      val postBooks: IO[Unit] =
        client.expect[Unit](POST(scenario.books.map(b => glomma.event.Book(b.name, b.price)).asJson, configuration.bookUrl))

      val postEvents: IO[Unit] = {
        def loop(events: LazyList[Event]): IO[Unit] =
          if(events.isEmpty) IO(())
          else client.expect[Unit](POST(events.head.asJson, configuration.eventUrl)).flatMap(_ => loop(events.tail))

        loop(evts)
      }

      (postBooks *> postEvents).as(ExitCode.Success)
    }
  }
}
