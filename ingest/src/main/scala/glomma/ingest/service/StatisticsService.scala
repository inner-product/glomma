package glomma.ingest.service

import scala.concurrent.Future

class StatisticsService() {
  // Most frequently viewed books
  private val frequentViews = new FrequentItemService[String](20)
  // Most frequently purchased books
  private val frequentPurchases = new FrequentItemService[String](20)
  // Most frequent customers
  private val frequentCustomers = new FrequentItemService[String](20)

  def addView(bookName: String): Unit =
    frequentViews.add(bookName)

  def addPurchase(bookName: String): Unit =
    frequentPurchases.add(bookName)

  /** Observe that a customer started a session */
  def addCustomer(customerName: String): Unit =
    frequentCustomers.add(customerName)

  def getViews: Future[Array[(String, Long)]] = ???
  def getPurchases: Future[Array[(String, Long)]] = ???
  def getCustomers: Future[Array[(String, Long)]] = ???
}
