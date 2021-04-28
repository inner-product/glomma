package glomma.ingest.service

import glomma.event._
import glomma.event.Event.SessionStart
import glomma.event.Event.View
import glomma.event.Event.Purchase
import glomma.event.rule.Rule
import cats.data.NonEmptyList

// Validate events
class ValidationService(books: BookService, sessions: SessionService) {


  def validate(event: Event): Either[NonEmptyList[String], Event] =
    event match {
	    case SessionStart(id, customerId, customerName) => ???
	    case View(sessionId, bookName) => ???
	    case Purchase(sessionId, bookName, bookPrice) => ???
    }
}
object ValidationService {

  type ValidationRule[A] = Rule[A, NonEmptyList[String]]

  def stringNotEmpty(message: String): ValidationRule[String] =
    Rule(message)((x: String) => x.nonEmpty)

}
