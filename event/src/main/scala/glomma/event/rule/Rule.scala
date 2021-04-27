package glomma.event.rule

import cats.data.NonEmptyList
import cats.implicits._
import cats.{Monoid, Semigroup, Semigroupal}

sealed trait Rule[A, E] {
  import Rule._

  def and(that: Rule[A, E]): Rule[A, E] =
    And(this, that)

  def or(that: Rule[A, E]): Rule[A, E] =
    Or(this, that)

  def product[B](that: Rule[B, E]): Rule[(A, B), E] =
    Product(this, that)

  def apply(input: A)(implicit m: Semigroup[E]): Either[E, A]
}
object Rule {
  final case class And[A, E](left: Rule[A, E], right: Rule[A, E])
      extends Rule[A, E] {
    def apply(input: A)(implicit m: Semigroup[E]): Either[E, A] =
      (left(input), right(input)).parMapN((_, _) => input)
  }
  final case class Or[A, E](left: Rule[A, E], right: Rule[A, E])
      extends Rule[A, E] {
    def apply(input: A)(implicit m: Semigroup[E]): Either[E, A] =
      (left(input), right(input)) match {
        case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
        case (Left(_), Right(_))  => input.asRight
        case (Right(_), Left(_))  => input.asRight
        case (Right(_), Right(_)) => input.asRight
      }
  }
  final case class Product[A, B, E](left: Rule[A, E], right: Rule[B, E])
      extends Rule[(A, B), E] {
    def apply(input: (A, B))(implicit m: Semigroup[E]): Either[E, (A, B)] = {
      val (a, b) = input
      (left(a), right(b)).parTupled
    }
  }
  final case class Literal[A, E](f: A => Either[E, A]) extends Rule[A, E] {
    def apply(input: A)(implicit m: Semigroup[E]): Either[E, A] =
      f(input)
  }

  implicit def ruleSemigroupal[E]: Semigroupal[Rule[*, E]] =
    new Semigroupal[Rule[*, E]] {
      def product[A, B](fa: Rule[A, E], fb: Rule[B, E]): Rule[(A, B), E] =
        fa.product(fb)
    }

  implicit def ruleMonoid[A, E]: Monoid[Rule[A, E]] =
    new Monoid[Rule[A, E]] {
      def combine(x: Rule[A, E], y: Rule[A, E]): Rule[A, E] = x.and(y)
      val empty: Rule[A, E] = Rule.always[A, E]
    }

  /** The Rule that always succeeds */
  def always[A, E]: Rule[A, E] = Rule(a => a.asRight)

  def apply[A, E](f: A => Either[E, A]): Rule[A, E] =
    Literal(f)

  def apply[A, E](
      message: E
  )(predicate: A => Boolean): Rule[A, NonEmptyList[E]] =
    Literal[A, NonEmptyList[E]](input =>
      if (predicate(input)) input.asRight else NonEmptyList.one(message).asLeft
    )
}
