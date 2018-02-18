# Applicative Syntax Plugin

This project implements a (sketchy, experimental) compiler plugin
that adds an opt-in applicative-style desugaring for `for` comprehensions.

```scala
import cats.implicits._
import cats.data.Validated

@applicative
val result: Validated[Int] = for {
  x <- Validated.validNel[String, Int](1)
  y <- Validated.validNel[String, Int](2)
  z <- Validated.invalidNel[String, Int]("validation failure!")
} yield x + y + z
```

(See the tests for more usage examples.)

## How does it work?

Scala compiler plugins don't have access to the 'sugary' form of `for` comprehensions:
by the time the parsing phase is complete,
the `for` syntax has already been desugared into a bunch of nested `flatMap` calls.
Thankfully, the structure of the desugared tree is fairly predictable.
This plugin looks for the `@applicative` annotation,
and when it finds it,
rewrites the annotated value to use applicative methods instead of monadic ones.

Of course, not all monadic code can be rewritten as applicative:
in particular,
_none_ of the values on the left-hand side of the `<-`s are visible on the right-hand side,
so code like this won't compile.

```scala
@applicative
val result: Validated[Int] = for {
  x <- Validated.validNel[String, Int](1)
  y <- Validated.validNel[String, Int](x) // Trying to use x in the definition of y!
} yield x + y
```

However, this plugin shouldn't change the meaning of code
where the applicative and monadic methods are consistent with each other.

## Why would anyone want this?

There are lots of reasons to write code in an applicative style:
many nice abstractions (like `Validated`) aren't monadic,
and others (like `Future`) might have better performance when used in an explicitly applicative fashion.

`cats` already supports a `mapN` syntax for applicative code;
using that, our first example looks like:

```scala
(
  Validated.validNel[String, Int](1),
  Validated.validNel[String, Int](2),
  Validated.invalidNel[String, Int]("validation failure!")
) mapN { (x, y, z) => x + y + z }
```

This gets a bit unwieldy as the number of values become large:
it's easy to lose track of which value gets bound to which name.
Recycling the `for` comprehension syntax makes the visual association much stronger.

## What's wrong with this project?

Unlike `flatMap`,
there's no standard naming in Scala code for applicative operations:
some types use `product` or `ap`, others `join` or `zip`,
and others only implement `flatMap`.
This makes it hard to write an applicative desugaring
that works with most code out of the box.
This project throws in the towel and just desugars to a cats-style `(...).mapN` call...
which means you'll need to have `cats.implicits._` in scope to use it.
(Desugaring to `product` calls might be a little bit more robust;
desugaring to `ap` would be ideal but has miserable type inference.)

Since this plugin runs at a very early phase of the compiler,
we don't have any access to type information.
If you already define and use an `@applicative` annotation somewhere in your code,
this plugin will break it.

## Can I try it out anyways?

Sure! It's not published anywhere yet,
but you should be able to copy the generated jar in your project.
Please let me know if you try this!