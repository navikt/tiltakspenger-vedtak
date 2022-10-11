package no.nav.tiltakspenger.felles

import io.kotest.assertions.throwables.shouldThrowWithMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UlidTest {

    @Test
    fun `test roundtrip`() {
        val prefix = "HH"
        val ulid = Ulid.new(prefix)
        val ulid2 = Ulid.fromDb(ulid.asString())
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test prefixPart and ulidPart`() {
        val prefix = "HH"
        val ulid = Ulid.new(prefix)
        val ulid2 = Ulid.fromDb("${ulid.prefixPart()}_${ulid.ulidPart()}")
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test fromDb negativ test`() {
        val utenGyldigSkilletegn = "HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av to deler skilt med _") {
            Ulid.fromDb(utenGyldigSkilletegn)
        }

        val utenPrefiks = "_HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, prefiks er tom") {
            Ulid.fromDb(utenPrefiks)
        }

        val utenUlid = "HH_"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, ulid er ugyldig") {
            Ulid.fromDb(utenUlid)
        }

        val ugyldigUlid = "HH_UU_JJ"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av prefiks + ulid") {
            Ulid.fromDb(ugyldigUlid)
        }
    }
}
