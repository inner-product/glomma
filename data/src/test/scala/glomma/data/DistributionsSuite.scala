package glomma.data

class DistributionsSuite extends munit.FunSuite {
  test("name generates famous names first") {
    val n = Distributions.name()
    val samples = n.sampleN(Data.famous.size)
    assertEquals(samples, Data.famous)
  }
}
