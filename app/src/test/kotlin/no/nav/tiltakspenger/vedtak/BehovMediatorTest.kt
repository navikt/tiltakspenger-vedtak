package no.nav.tiltakspenger.vedtak

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.Aktivitetslogg.Aktivitet.Behov.Behovtype
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BehovMediatorTest {
    private companion object {
        private const val ident = "ident"
        private lateinit var behovMediator: BehovMediator
    }

    private val testRapid = TestRapid()
    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var søker: Søker

    @BeforeEach
    fun setup() {
        søker = Søker(ident = ident)
        aktivitetslogg = Aktivitetslogg()
        behovMediator = BehovMediator(
            rapidsConnection = testRapid
        )
        testRapid.reset()
    }

    @Test
    internal fun `grupperer behov`() {

        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(søker)
        hendelse.behov(
            Behovtype.persondata,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344"
            )
        )
        hendelse.behov(Behovtype.arenatiltak, "Trenger Arenatiltak")
        hendelse.behov(Behovtype.skjermingdata, "Trenger Skjermingdata")

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        assertEquals(1, inspektør.size)
        assertEquals(ident, inspektør.key(0))

        inspektør.message(0).also {
            assertEquals("behov", it["@event_name"].asText())
            assertTrue(it.hasNonNull("@id"))
            assertDoesNotThrow { UUID.fromString(it["@id"].asText()) }
            assertTrue(it.hasNonNull("@opprettet"))
            assertDoesNotThrow { LocalDateTime.parse(it["@opprettet"].asText()) }
            assertEquals(listOf("persondata", "arenatiltak", "skjermingdata"), it["@behov"].map(JsonNode::asText))
            assertEquals("behov", it["@event_name"].asText())
            assertEquals("12344", it["aktørId"].asText())
            assertEquals(ident, it["ident"].asText())
        }
    }

    @Test
    internal fun `sjekker etter duplikatverdier`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(søker)
        hendelse.behov(
            Behovtype.persondata,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344"
            )
        )
        hendelse.behov(
            Behovtype.persondata,
            "Trenger personopplysninger",
            mapOf(
                "aktørId" to "12344"
            )
        )

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    @Test
    internal fun `kan ikke produsere samme behov`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.setForelderAndAddKontekst(søker)
        hendelse.behov(Behovtype.arenatiltak, "Trenger Arenatiltak")
        hendelse.behov(Behovtype.arenatiltak, "Trenger Arenatiltak")

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    private class Testkontekst(
        private val melding: String
    ) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() = SpesifikkKontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        private val melding: String,
        internal val logg: Aktivitetslogg
    ) : Hendelse(logg), Aktivitetskontekst {
        init {
            logg.addKontekst(this)
        }

        override fun ident(): String = ident

        override fun toSpesifikkKontekst() = SpesifikkKontekst("TestHendelse")
        override fun addKontekst(kontekst: Aktivitetskontekst) {
            logg.addKontekst(kontekst)
        }
    }
}
