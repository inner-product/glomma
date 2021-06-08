package glomma.ingest.service

import cats.effect.IO
import cats.implicits._
import glomma.data.data._
import glomma.data.random._
import glomma.event
import glomma.event.Event
import munit._

class ValidationServiceSuite extends CatsEffectSuite {
  // Sample a scenario used to generate our other data
  val scenario = Scenarios.small.sample()
  val badEvent = BadEvent(scenario)

  // Sample 1000 invalid events
  def badEvents = badEvent.generate.sampleN(1000)

  // All the valid events from the scenario
  val goodEvents = scenario.sessions.flatMap(_.toEvents.toList)

  val validationService = {
    for {
      bookService <- IO(new BookService())
      _ = bookService.addBooks(
        scenario.books.map(b => event.Book(b.name, b.price))
      )
      sessionService <- SessionService()
      _ <- scenario.sessions.foldMapM(s =>
        sessionService.addSession(
          Event.SessionStart(
            s.sessionId,
            s.customer.customerId,
            s.customer.name
          )
        )
      )
    } yield ValidationService(bookService, sessionService)
  }

  test("All bad events are invalid") {
    validationService.flatMap { service =>
      badEvents.foldMapM(evt =>
        service
          .validate(evt)
          .map(either =>
            assert(either.isLeft, s"$evt was not marked as invalid")
          )
      )
    }
  }

  test("All good events are valid") {
    validationService.flatMap { service =>
      goodEvents.foldMapM(evt =>
        service.validate(evt).map(either => assertEquals(either, Right(evt)))
      )
    }
  }
}
