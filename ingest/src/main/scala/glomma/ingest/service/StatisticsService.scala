package glomma.ingest.service

import cats.effect.IO
import cats.effect.kernel.GenConcurrent
import cats.implicits._

class StatisticsService(
    val frequentViews: FrequentItemService[String],
    val frequentPurchases: FrequentItemService[String],
    val frequentCustomers: FrequentItemService[String]
) {

  def addView(bookName: String): IO[Unit] =
    frequentViews.add(bookName)

  def addPurchase(bookName: String): IO[Unit] =
    frequentPurchases.add(bookName)

  /** Observe that a customer started a session */
  def addCustomer(customerName: String): IO[Unit] = {
    customerName.size
    IO.unit
  }

  def getViews: IO[IndexedSeq[(String, Long)]] =
    frequentViews.get.map(arr => arr.toIndexedSeq)
  def getPurchases: IO[IndexedSeq[(String, Long)]] =
    frequentPurchases.get.map(arr => arr.toIndexedSeq)
  def getCustomers: IO[IndexedSeq[(String, Long)]] =
    frequentCustomers.get.map(arr => arr.toIndexedSeq)
}
object StatisticsService {
  def apply()(implicit f: GenConcurrent[IO, _]): IO[StatisticsService] =
    (
      FrequentItemService[String](20),
      FrequentItemService[String](20),
      FrequentItemService[String](20)
    ).mapN {
      new StatisticsService(_, _, _)
    }
}
