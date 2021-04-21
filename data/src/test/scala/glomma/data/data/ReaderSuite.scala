package glomma.data.data

class ReaderSuite extends munit.FunSuite {
  test("Reader succeeds when reading a known file") {
    val lines = Reader.read("famous.txt")
    assert(lines.size > 0)
  }
}
