package com.monovore.syntax

import cats.data.{NonEmptyList, Validated}
import org.scalatest.WordSpec
import cats.implicits._

class ApplicativeSpec extends WordSpec {

  "Applicative syntax" should {

    "work for options" in {

      @applicative
      val result =
        for {
          x <- Option(1)
          y <- Option(1)
        } yield x + y

      assert(result == Some(2))
    }

    "work for validated types" in {

      @applicative
      val result = for {
        x <- Validated.validNel[String, Int](1)
        y <- Validated.validNel[String, Int](2)
        z <- Validated.validNel[String, Int](3)
      } yield List(x, y, z)

      assert(result == Validated.validNel(List(1, 2, 3)))
    }

    "work for validated types with failures" in {

      @applicative
      val result = for {
        x <- Validated.invalidNel[String, Int]("dang")
        y <- Validated.validNel[String, Int](1)
        z <- Validated.invalidNel[String, Int]("yikes")
      } yield x + y + z

      assert(result == Validated.invalid(NonEmptyList("dang", List("yikes"))))
    }
  }

}
