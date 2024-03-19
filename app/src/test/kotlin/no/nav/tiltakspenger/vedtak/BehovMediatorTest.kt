package no.nav.tiltakspenger.vedtak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg.Aktivitet.Behov.Behovtype
import no.nav.tiltakspenger.innsending.domene.Innsending
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.Kontekst
import no.nav.tiltakspenger.innsending.domene.KontekstLogable
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMediatorTest {
    private companion object {
        private const val journalpostId = "journalpostId"
        private const val ident = "ident"
        private lateinit var behovMediator: BehovMediator
    }

    private val testRapid = TestRapid()
    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var innsending: Innsending

    @BeforeEach
    fun setup() {
        innsending = Innsending(journalpostId = journalpostId, ident = ident, fom = 1.januar(2022), tom = 31.mars(2022))
        aktivitetslogg = Aktivitetslogg()
        behovMediator = BehovMediator(
            rapidsConnection = testRapid,
        )
        testRapid.reset()
    }

    @Test
    internal fun `grupperer behov`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(innsending)
        hendelse.behov(
            Behovtype.personopplysninger,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344",
            ),
        )
        hendelse.behov(Behovtype.tiltak, "Trenger tiltak")
        hendelse.behov(Behovtype.skjerming, "Trenger Skjermingdata")

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        assertEquals(1, inspektør.size)
        assertEquals(journalpostId, inspektør.key(0))

        inspektør.message(0).also {
            assertEquals("behov", it["@event_name"].asText())
            assertTrue(it.hasNonNull("@id"))
            assertDoesNotThrow { UUID.fromString(it["@id"].asText()) }
            assertTrue(it.hasNonNull("@opprettet"))
            assertDoesNotThrow { LocalDateTime.parse(it["@opprettet"].asText()) }
            assertEquals(listOf("personopplysninger", "tiltak", "skjerming"), it["@behov"].map(JsonNode::asText))
            assertEquals("behov", it["@event_name"].asText())
            assertEquals("12344", it["aktørId"].asText())
            assertEquals(journalpostId, it["journalpostId"].asText())
        }
    }

    @Test
    internal fun `sjekker etter duplikatverdier`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(innsending)
        hendelse.behov(
            Behovtype.personopplysninger,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344",
            ),
        )
        hendelse.behov(
            Behovtype.personopplysninger,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344",
            ),
        )

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    @Test
    internal fun `kan ikke produsere samme behov`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(innsending)
        hendelse.behov(Behovtype.tiltak, "Trenger tiltak")
        hendelse.behov(Behovtype.tiltak, "Trenger tiltak")

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    private class Testkontekst(
        private val melding: String,
    ) : KontekstLogable {
        override fun opprettKontekst() = Kontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        private val melding: String,
        internal val logg: Aktivitetslogg,
    ) : InnsendingHendelse(logg), KontekstLogable {
        init {
            logg.addKontekst(this)
        }

        override fun journalpostId(): String = journalpostId

        override fun opprettKontekst() = Kontekst("TestHendelse")
        override fun addKontekst(kontekst: KontekstLogable) {
            logg.addKontekst(kontekst)
        }
    }
}
