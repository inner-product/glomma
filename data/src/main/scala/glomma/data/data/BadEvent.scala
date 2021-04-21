package glomma.data.data

import java.util.UUID

import glomma.data.random._
import glomma.event.Event

// Generate invalid events
final case class BadEvent(scenario: Scenario) {
  val book: Random[Book] = Random.oneOfSeq(scenario.books)
  val session: Random[Session] = Random.oneOfSeq(scenario.sessions)

  val badSession: Random[Event] =
    Random.delay(Event.SessionStart(UUID.randomUUID(), UUID.randomUUID(), ""))

  val badView: Random[Event] =
    Random.oneOfM(
      // Invalid session but valid book
      book.map(b => Event.View(UUID.randomUUID(), b.name)),
      // Valid session but invalid book
      for {
        s <- session
        b <- BadEvent.missingBook
      } yield Event.View(s.sessionId, b.name),
      // Valid session but invalid book
      session.map(s => Event.View(s.sessionId, ""))
    )

  val badPurchase: Random[Event] =
    Random.oneOfM(
      // Invalid session but valid book
      book.map(b => Event.Purchase(UUID.randomUUID(), b.name, b.price)),
      // Valid session but no book
      session.map(s => Event.Purchase(s.sessionId, "", 0.0)),
      // Valid session but missing book
      for {
        s <- session
        b <- BadEvent.missingBook
      } yield Event.Purchase(s.sessionId, b.name, b.price),
      // Valid session but bad book price
      for {
        s <- session
        b <- book
      } yield Event.Purchase(s.sessionId, b.name, -b.price)
    )

  val generate: Random[Event] =
    Random.oneOfM(badSession, badView, badPurchase)
}
object BadEvent {
  // Books that aren't in the catalogue
  val missingBooks =
    List(
      Book("30 Blindingly Good Hjemmebrent Recipes", 12.99),
      Book("Brew Your Own Hjemmebrent", 7.99),
      Book("Leif Erikson's Guide to the Americas", 15.99),
      Book("Dentistry with Harald Bluetooth", 17.99),
      Book(
        "Fighting, Pillaging, and Sacrifice: a Viking's Guide to the Good Life",
        12.99
      )
    )

  val missingBook: Random[Book] = Random.oneOf(missingBooks: _*)
}
