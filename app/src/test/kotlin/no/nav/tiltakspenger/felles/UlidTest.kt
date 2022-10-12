package no.nav.tiltakspenger.felles

import io.kotest.assertions.throwables.shouldThrowWithMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UlidTest {

    @Test
    fun `test roundtrip`() {
        val ulid = SøkerId.random()
        val ulid2 = SøkerId.fromDb(ulid.toString())
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test prefixPart and ulidPart`() {
        val ulid = SøkerId.random()
        val ulid2 = SøkerId.fromDb("${ulid.prefixPart()}_${ulid.ulidPart()}")
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test fromDb negativ test`() {
        val utenGyldigSkilletegn = "HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av to deler skilt med _") {
            SøkerId.fromDb(utenGyldigSkilletegn)
        }

        val utenPrefiks = "_HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, prefiks er tom") {
            SøkerId.fromDb(utenPrefiks)
        }

        val utenUlid = "HH_"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, ulid er ugyldig") {
            SøkerId.fromDb(utenUlid)
        }

        val ugyldigUlid = "HH_UU_JJ"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av prefiks + ulid") {
            SøkerId.fromDb(ugyldigUlid)
        }
    }
}
