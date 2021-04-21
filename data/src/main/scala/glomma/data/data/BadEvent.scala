package glomma.data.data

import glomma.data.random._
import glomma.event.Event
import java.util.UUID

// Generate invalid events
final case class BadEvent(scenario: Scenario) {
  def generate: Random[Event] =
    Random.oneOf(session, view, purchase)

  def session: Random[Event.Session] =
    Random.delay(Event.Session(UUID.randomUUID(), UUID.randomUUID(), ""))

  def view: Random[Event.View] = ???

  def purchase: Random[Event.Purchase] = ???
}
object BadEvent {
  val badBooks =
    List(
      Book("30 Blindingly Good Hjemmebrent Recipes", 12.99),
      Book("Brew Your Own Hjemmebrent", 7.99),
      Book("Leif Erikson's Guide to the Americas", 15.99),
      Book("Dentistry with Harald Bluetooth", 17.99),
      Book("Fighting, Pillaging, and Sacrifice: a Viking's Guide to the Good Life", 12.99)
    )

  val badBook: Random[Book] = Random.oneOf(badBooks: _*)
}
