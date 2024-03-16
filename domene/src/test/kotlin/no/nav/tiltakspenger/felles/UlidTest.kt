package no.nav.tiltakspenger.felles

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

internal class UlidTest {

    @Test
    fun `test roundtrip`() {
        val ulid = BehandlingId.random()
        val ulid2 = BehandlingId.fromString(ulid.toString())
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test prefixPart and ulidPart`() {
        val ulid = BehandlingId.random()
        val ulid2 = BehandlingId.fromString("${ulid.prefixPart()}_${ulid.ulidPart()}")
        assertEquals(ulid, ulid2)
    }

    @Test
    fun `test fromDb negativ test`() {
        val utenGyldigSkilletegn = "HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av to deler skilt med _") {
            BehandlingId.fromString(utenGyldigSkilletegn)
        }

        val utenPrefiks = "_HH"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, prefiks er tom") {
            BehandlingId.fromString(utenPrefiks)
        }

        val utenUlid = "HH_"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, ulid er ugyldig") {
            BehandlingId.fromString(utenUlid)
        }

        val ugyldeTegniUlid = "HH_1234567890123456789U123456"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, ulid er ugyldig") {
            BehandlingId.fromString(ugyldeTegniUlid)
        }

        val ugyldigUlid = "HH_UU_JJ"
        shouldThrowWithMessage<IllegalArgumentException>("Ikke gyldig Id, skal bestå av prefiks + ulid") {
            BehandlingId.fromString(ugyldigUlid)
        }
    }

    @Test
    fun `test konvertering av BehandlingId til UUID og tilbake igjen til ULID`() {
        repeat(100) {
            val opprinneligBehandlingId = BehandlingId.random()
            val uuid = opprinneligBehandlingId.uuid()
            val behandlingIdFraUUID = BehandlingId.fromUUID(uuid)

            opprinneligBehandlingId shouldBe behandlingIdFraUUID
        }
    }

    @Test
    fun `test compareTo av ULID`() {
        repeat(100) {
            val førsteId = SakId.random()
            sleep(1)
            val andreId = SakId.random()

            andreId shouldBeGreaterThan førsteId
        }
    }
}
