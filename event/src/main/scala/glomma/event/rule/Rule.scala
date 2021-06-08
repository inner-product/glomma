package glomma.event.rule

import cats._
import cats.data.NonEmptyList
import cats.implicits._

sealed trait Rule[F[_], A, E] {
  import Rule._

  def and(that: Rule[F, A, E]): Rule[F, A, E] =
    And(this, that)

  def or(that: Rule[F, A, E]): Rule[F, A, E] =
    Or(this, that)

  def product[B](that: Rule[F, B, E]): Rule[F, (A, B), E] =
    Product(this, that)

  def contramap[B](f: B => A): Rule[F, B, E] =
    Contramap(this, f)

  def apply(
      input: A
  )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, A]]
}
object Rule {
  final case class And[F[_], A, E](left: Rule[F, A, E], right: Rule[F, A, E])
      extends Rule[F, A, E] {
    def apply(
        input: A
    )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, A]] =
      right(input)
  }
  final case class Or[F[_], A, E](left: Rule[F, A, E], right: Rule[F, A, E])
      extends Rule[F, A, E] {
    def apply(
        input: A
    )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, A]] =
      (left(input), right(input)).mapN { (eA, eB) =>
        (eA, eB) match {
          case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
          case (Left(_), Right(_))  => input.asRight
          case (Right(_), Left(_))  => input.asRight
          case (Right(_), Right(_)) => input.asRight
        }
      }
  }
  final case class Product[F[_], A, B, E](
      left: Rule[F, A, E],
      right: Rule[F, B, E]
  ) extends Rule[F, (A, B), E] {
    def apply(
        input: (A, B)
    )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, (A, B)]] = {
      val (a, b) = input
      (left(a), right(b)).mapN((eA, eB) => (eA, eB).parTupled)
    }
  }
  final case class Contramap[F[_], A, B, E](
      source: Rule[F, A, E],
      f: B => A
  ) extends Rule[F, B, E] {
    def apply(
        input: B
    )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, B]] = {
      source(f(input)).map(_.as(input))
    }
  }
  final case class Literal[F[_], A, E](f: A => F[Either[E, A]])
      extends Rule[F, A, E] {
    def apply(
        input: A
    )(implicit s: Applicative[F], m: Semigroup[E]): F[Either[E, A]] =
      f(input)
  }

  implicit def ruleSemigroupal[F[_], E]: Semigroupal[Rule[F, *, E]] =
    new Semigroupal[Rule[F, *, E]] {
      def product[A, B](
          fa: Rule[F, A, E],
          fb: Rule[F, B, E]
      ): Rule[F, (A, B), E] =
        fa.product(fb)
    }

  implicit def ruleContravariant[F[_], E]: Contravariant[Rule[F, *, E]] =
    new Contravariant[Rule[F, *, E]] {
      def contramap[A, B](
          fa: Rule[F, A, E]
      )(f: B => A): Rule[F, B, E] =
        fa.contramap(f)
    }

  implicit def ruleMonoid[F[_]: Applicative, A, E]: Monoid[Rule[F, A, E]] =
    new Monoid[Rule[F, A, E]] {
      def combine(x: Rule[F, A, E], y: Rule[F, A, E]): Rule[F, A, E] = x.and(y)
      val empty: Rule[F, A, E] = Rule.always[F, A, E]
    }

  /** The Rule that always succeeds */
  def always[F[_]: Applicative, A, E]: Rule[F, A, E] =
    Rule(a => a.asRight.pure[F])

  def apply[F[_], A, E](f: A => F[Either[E, A]]): Rule[F, A, E] =
    Literal(f)

  def apply[F[_]: Functor, A, E](
      message: E
  )(predicate: A => F[Boolean]): Rule[F, A, NonEmptyList[E]] =
    Literal[F, A, NonEmptyList[E]](input =>
      predicate(input).map(bool =>
        if (bool) input.asRight else NonEmptyList.one(message).asLeft
      )
    )

  final case class RulePureBuilder[F[_]]() {
    def apply[A, E](message: E)(
        predicate: A => Boolean
    )(implicit ap: Applicative[F]): Rule[F, A, NonEmptyList[E]] =
      Literal[F, A, NonEmptyList[E]](input =>
        if (predicate(input)) input.asRight.pure[F]
        else NonEmptyList.one(message).asLeft.pure[F]
      )

    def apply[A, E](f: A => Either[E, A])(implicit
        ap: Applicative[F]
    ): Rule[F, A, E] =
      Literal(a => f(a).pure[F])
  }
  def pure[F[_]]: RulePureBuilder[F] =
    RulePureBuilder[F]()
}
