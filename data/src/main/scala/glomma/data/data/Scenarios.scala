package glomma.data.data

object Scenarios {

  /** Small scenario for testings */
  val small = Distributions.makeScenario(100, 5, 1000)

  /** Large scenario for the real thing */
  val large = Distributions.makeScenario(10000, 20, 1000000)
}
