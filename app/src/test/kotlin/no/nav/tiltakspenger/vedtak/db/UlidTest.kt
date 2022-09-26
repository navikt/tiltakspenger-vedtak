package no.nav.tiltakspenger.vedtak.db

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
}
