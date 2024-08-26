package no.nav.tiltakspenger.common

import arrow.core.Either
import arrow.core.getOrElse
import io.kotest.assertions.fail

fun <A, B> Either<A, B>.getOrFail(msg: String): B {
    return getOrElse { fail("Message: $msg, Error: $it") }
}

fun <A, B> Either<A, B>.getOrFail(): B {
    return getOrElse { fail("""$it""") }
}
