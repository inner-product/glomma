package glomma.ingest.service

import glomma.data.data._
import glomma.data.random._
import munit._

class ValidationServiceSuite extends FunSuite {
  // Sample a scenario used to generate our other data
  val scenario = Scenarios.small.sample()
  val badEvent = BadEvent(scenario)

  // Sample 1000 invalid events
  def badEvents = badEvent.generate.sampleN(1000)

  // All the valid events from the scenario
  val goodEvents = scenario.sessions.flatMap(_.toEvents.toList)

  // Uncomment the below and implement validationService
  // val validationService: ValidationService = ???

  // test("All bad events are invalid"){
  //   badEvents.map(evt => assert(validationService.validate(evt).isLeft))
  // }

  // test("All good events are valid"){
  //   goodEvents.map(evt => assert(validationService.validate(evt).isRight))
  // }
}
