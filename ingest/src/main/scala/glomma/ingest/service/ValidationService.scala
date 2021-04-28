package glomma.ingest.service

import glomma.event._

// Validate events
class ValidationService() {
  // String is the used as the error below so that the code compiles. You will
  // probably want to change this type in your code.
  def validate(event: Event): Either[String, Event] =
    ???
}
