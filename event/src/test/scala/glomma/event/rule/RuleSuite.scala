package glomma.event.rule

import cats.Id
import cats.data.NonEmptyList
import cats.implicits._
import munit._

class RuleSuite extends FunSuite {
  test("Literal from message and predicate succeeds and fails as expected") {
    val msg = "It wasn't 3"
    val rule = Rule.pure[Id](msg)((x: Int) => x == 3)
    assertEquals(rule(3), Right(3))
    assertEquals(rule(-1), Left(NonEmptyList.one(msg)))
  }

  test("Literal succeeds and fails as expected") {
    val msg = "It wasn't 3"
    val rule =
      Rule.pure[Id]((x: Int) =>
        if (x == 3) Right(3) else Left(NonEmptyList.one(msg))
      )
    assertEquals(rule(3), Right(3))
    assertEquals(rule(-1), Left(NonEmptyList.one(msg)))
  }

  test("And fails when either rule fails") {
    val msg1 = "Kaboom!"
    val msg2 = "Kablooie!"

    val rule1 = Rule.pure[Id](msg1)((x: Int) => x < 4)
    val rule2 = Rule.pure[Id](msg2)((x: Int) => x < 2)
    val rule = rule1.and(rule2)

    assertEquals(rule(5), Left(NonEmptyList.of(msg1, msg2)))
    assertEquals(rule(3), Left(NonEmptyList.one(msg2)))
    assertEquals(rule(1), Right(1))
  }

  test("Or fails when both rules fail") {
    val msg1 = "Kaboom!"
    val msg2 = "Kablooie!"

    val rule1 = Rule.pure[Id](msg1)((x: Int) => x < 4)
    val rule2 = Rule.pure[Id](msg2)((x: Int) => x < 2)
    val rule = rule1.or(rule2)

    assertEquals(rule(3), Right(3))
    assertEquals(rule(1), Right(1))
    assertEquals(rule(5), Left(NonEmptyList.of(msg1, msg2)))
  }

  test("Product fails when either rule fails") {
    val msg1 = "Kaboom!"
    val msg2 = "Kablooie!"

    val rule1 = Rule.pure[Id](msg1)((x: Int) => x <= 2)
    val rule2 = Rule.pure[Id](msg2)((x: Int) => x <= 2)
    val rule = rule1.product(rule2)

    assertEquals(rule((3, 1)), Left(NonEmptyList.one(msg1)))
    assertEquals(rule((1, 3)), Left(NonEmptyList.one(msg2)))
    assertEquals(rule((2, 2)), Right((2, 2)))
  }

  test("Contrmap applies transform before applying rule") {
    val msg = "Kaboom!"
    val rule = Rule.pure[Id](msg)((x: Int) => x <= 2).contramap[Double](_.toInt)

    assertEquals(rule(3.0), Left(NonEmptyList.one(msg)))
    assertEquals(rule(1.0), Right(1.0))
  }

  test("Rule monoid instance is and") {
    val msg1 = "Kaboom!"
    val msg2 = "Kablooie!"

    val rule1 = Rule.pure[Id](msg1)((x: Int) => x < 4)
    val rule2 = Rule.pure[Id](msg2)((x: Int) => x < 2)
    val rule = rule1 |+| rule2

    assertEquals(rule(5), Left(NonEmptyList.of(msg1, msg2)))
    assertEquals(rule(3), Left(NonEmptyList.one(msg2)))
    assertEquals(rule(1), Right(1))
  }
}
