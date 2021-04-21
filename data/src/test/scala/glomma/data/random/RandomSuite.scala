package glomma.data.random

class RandomSuite extends munit.FunSuite {
  test("always generate the single value") {
    assertEquals(Random.always(1).sampleN(10), List.fill(10)(1))
  }

  test("natural is always within range") {
    val samples = Random.natural(10).sampleN(100)
    assertEquals(samples.size, 100)
    assert(samples.forall(x => 0 <= x && x < 10))
  }

  test("weighted selects single weighted element") {
    val samples = Random.weighted("a" -> 1, "b" -> 0, "c" -> 0).sampleN(100)
    assertEquals(samples.size, 100)
    assert(samples.forall(x => x == "a"))
  }

  test("weighted selects between elements with positive weight") {
    val samples = Random.weighted("a" -> 1, "b" -> 0, "c" -> 1).sampleN(100)
    assertEquals(samples.size, 100)
    assert(samples.forall(x => x == "a" || x == "c"))
  }

  test("fold generates deterministic sequence") {
    val d = Random.fold(0)((_, state) =>
      if (state == 4) (4, 0)
      else (state, state + 1)
    )
    val samples = d.sampleN(10)
    assertEquals(samples, List(0, 1, 2, 3, 4, 0, 1, 2, 3, 4))
  }

  test("sticking breaking assigns all weight to single element") {
    val d = Random.finiteStickBreaking("a")
    val samples = d.sampleN(1000)
    assert(samples.forall(x => x == "a"))
  }

  test("stick breaking distributes weight amongst elements") {
    val d = Random.finiteStickBreaking(1, 2, 3, 4)
    val samples = d.sampleN(1000, new scala.util.Random(1234))
    assert(samples.forall(x => 1 <= x && x <= 4))
    assertEquals(samples.distinct.sorted, List(1, 2, 3, 4))
  }
}
