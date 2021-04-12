package glomma.data

class DistributionsSuite extends munit.FunSuite {
  test("name generates famous names first") {
    val n = Distributions.name()
    val samples = n.sampleN(Data.famous.size)
    assertEquals(samples, Data.famous)
  }

  test("makeCustomer generates plausible customers") {
    val c = Distributions.makeCustomer(Distributions.makeCluster(10))
    val samples = c.sampleN(1000)

    assert(samples.map(c => c.name).distinct.size >= 100)
    assertEquals(samples.map(c => c.customerId).distinct.size, 1000)
    assertEquals(samples.map(c => c.cluster).distinct.sorted, List.range(0, 10))
  }

  test("makeCustomers generates plausible customers") {
    val c = Distributions.makeCustomers(
      1000,
      Distributions.makeCustomer(Distributions.makeCluster(10))
    )
    val samples = c.sample()

    assert(samples.map(c => c.name).distinct.size >= 100)
    assertEquals(samples.map(c => c.customerId).distinct.size, 1000)
    assertEquals(samples.map(c => c.cluster).distinct.sorted, List.range(0, 10))
  }

  test("makeScenario generates a plausible scenario") {
    val scenario = Distributions.makeScenario(10, 2, 100).sample()
    assertEquals(scenario.customers.size, 10)
    assertEquals(scenario.sessions.size, 100)
    assert(
      scenario.sessions.map(s => s.customer.name).distinct.size > 1,
      scenario.sessions.map(s => s.customer.name)
    )
    assertEquals(
      scenario.sessions.map(s => s.customer.customerId).distinct.size,
      10
    )
    assert(
      scenario.sessions.map(s => s.customer.cluster).distinct.size > 1,
      scenario.sessions.map(s => s.customer.cluster)
    )
    assert(
      scenario.sessions.map(s => s.purchased).distinct.size > 1,
      scenario.sessions.map(s => s.purchased)
    )
  }
}
