package no.nav.tiltakspenger.felles

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class NavkontorTest {

    @Test
    fun `skal ikke kaste `() {
        Navkontor("0000")
        Navkontor("0001")
        Navkontor("1234")
        Navkontor("9999")
    }

    @Test
    fun `Skal kaste dersom for få siffer`() {
        shouldThrow<IllegalArgumentException> {
            Navkontor("123")
        }
    }

    @Test
    fun `Skal kaste dersom for mange siffer`() {
        shouldThrow<IllegalArgumentException> {
            Navkontor("12345")
        }
    }

    @Test
    fun `Skal kaste dersom ulovlige tegn`() {
        shouldThrow<IllegalArgumentException> {
            Navkontor("123-")
        }
        shouldThrow<IllegalArgumentException> {
            Navkontor("123/")
        }
    }

    @Test
    fun `Skal kaste dersom bokstaver`() {
        shouldThrow<IllegalArgumentException> {
            Navkontor("123a")
        }
        shouldThrow<IllegalArgumentException> {
            Navkontor("123A")
        }
    }

    @Test
    fun `Skal kaste med negative`() {
        shouldThrow<IllegalArgumentException> {
            Navkontor("-123")
        }
        shouldThrow<IllegalArgumentException> {
            Navkontor("-1234")
        }
    }
}
