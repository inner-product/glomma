package glomma.ingest.service

import glomma.data.data._
import glomma.data.random._
import glomma.event
import glomma.event.Event
import munit._

class ValidationServiceSuite extends FunSuite {
  // Sample a scenario used to generate our other data
  val scenario = Scenarios.small.sample()
  val badEvent = BadEvent(scenario)

  // Sample 1000 invalid events
  def badEvents = badEvent.generate.sampleN(1000)

  // All the valid events from the scenario
  val goodEvents = scenario.sessions.flatMap(_.toEvents.toList)

  val bookService = new BookService()
  bookService.addBooks(scenario.books.map(b => event.Book(b.name, b.price)))

  val sessionService = new SessionService()
  scenario.sessions.foreach(s =>
    sessionService.addSession(
      Event.SessionStart(s.sessionId, s.customer.customerId, s.customer.name)
    )
  )

  val validationService: ValidationService =
    new ValidationService(bookService, sessionService)

  test("All bad events are invalid") {
    badEvents.map(evt =>
      assert(
        validationService.validate(evt).isLeft,
        s"$evt was not marked as invalid"
      )
    )
  }

  test("All good events are valid") {
    goodEvents.map(evt =>
      assertEquals(validationService.validate(evt), Right(evt))
    )
  }
}
