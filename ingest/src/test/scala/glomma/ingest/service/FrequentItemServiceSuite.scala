package glomma.ingest.service

import cats.implicits._
import glomma.data.data._
import glomma.data.random._
import munit._

class FrequentItemServiceSuite extends CatsEffectSuite {
  // Sample a scenario used to generate our other data
  val scenario = Scenarios.small.sample()

  implicit val runtime = cats.effect.unsafe.IORuntime.global
  val makeService = FrequentItemService[String](10)

  test("Most frequent customers") {
    for {
      service <- makeService
      _ <- scenario.sessions.foldMapM(session =>
        service.add(session.customer.name)
      )
      customers <- service.get
    } yield {
      assert(customers.size <= 10)
      println("-----------------------------------------")
      println("Frequent Customers")
      println("-----------------------------------------")
      customers.foreach { case (name, _) =>
        println(name)
      }
    }
  }

  test("Most frequently viewed books") {
    for {
      service <- makeService
      _ <- scenario.sessions.foldMapM(session =>
        session.viewed.foldMapM(view => service.add(view.book.name))
      )
      views <- service.get
    } yield {
      assert(views.size <= 10)
      println("-----------------------------------------")
      println("Frequently Viewed Books")
      println("-----------------------------------------")
      views.foreach { case (name, _) =>
        println(name)
      }
    }
  }

  test("Most frequently purchased books") {
    for {
      service <- makeService
      _ <- scenario.sessions.foldMapM(session =>
        session.purchased.foldMapM(book => service.add(book.name))
      )
      purchases <- service.get
    } yield {
      assert(purchases.size <= 10)
      println("-----------------------------------------")
      println("Frequently Purchased Books")
      println("-----------------------------------------")
      purchases.foreach { case (name, _) =>
        println(name)
      }
    }
  }
}
