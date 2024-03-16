package no.nav.tiltakspenger.innsending

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.IAktivitetslogg
import no.nav.tiltakspenger.innsending.domene.IAktivitetsloggVisitor
import no.nav.tiltakspenger.innsending.domene.Kontekst
import no.nav.tiltakspenger.innsending.domene.KontekstLogable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class AktivitetsloggTest {

    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var person: TestKontekst

    @BeforeEach
    fun setUp() {
        person = TestKontekst("Søker")
        aktivitetslogg = Aktivitetslogg()
    }

    @Test
    fun `inneholder original melding`() {
        val infomelding = "info message"
        aktivitetslogg.info(infomelding)
        assertInfo(infomelding)
    }

    @Test
    fun `har ingen feil ved default`() {
        assertFalse(aktivitetslogg.hasErrors())
    }

    @Test
    fun `severe oppdaget og kaster exception`() {
        val melding = "Severe error"
        assertThrows<Aktivitetslogg.AktivitetException> { aktivitetslogg.severe(melding) }
        assertTrue(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertSevere(melding)
    }

    @Test
    fun `error oppdaget`() {
        val melding = "Error"
        aktivitetslogg.error(melding)
        assertTrue(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertError(melding)
    }

    @Test
    fun `warning oppdaget`() {
        val melding = "Warning explanation"
        aktivitetslogg.warn(melding)
        assertFalse(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertWarn(melding)
    }

    @Test
    fun `Melding sendt til forelder`() {
        val hendelse = TestHendelse(
            "Hendelse",
            aktivitetslogg.barn(),
        )
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
        "error message".also {
            hendelse.error(it)
            assertError(it, hendelse.logg)
            assertError(it, aktivitetslogg)
        }
    }

    @Test
    fun `Melding sendt fra barnebarn til forelder`() {
        val hendelse = TestHendelse(
            melding = "Hendelse",
            logg = aktivitetslogg.barn(),
        )
        hendelse.addKontekst(person)
        val arbeidsgiver =
            TestKontekst("Melding")
        hendelse.addKontekst(arbeidsgiver)
        val vedtaksperiode =
            TestKontekst("Soknad")
        hendelse.addKontekst(vedtaksperiode)
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
        "error message".also {
            hendelse.error(it)
            assertError(message = it, aktivitetslogg = hendelse.logg)
            assertError(message = it, aktivitetslogg = aktivitetslogg)
            assertError(message = "Hendelse", aktivitetslogg = aktivitetslogg)
            assertError(message = "Soknad", aktivitetslogg = aktivitetslogg)
            assertError(message = "Melding", aktivitetslogg = aktivitetslogg)
            assertError(message = "Søker", aktivitetslogg = aktivitetslogg)
        }
    }

    @Test
    fun `Vis bare arbeidsgiveraktivitet`() {
        val hendelse1 = TestHendelse(
            "Hendelse1",
            aktivitetslogg.barn(),
        )
        hendelse1.addKontekst(person)
        val arbeidsgiver1 =
            TestKontekst("Arbeidsgiver 1")
        hendelse1.addKontekst(arbeidsgiver1)
        val vedtaksperiode1 =
            TestKontekst("Vedtaksperiode 1")
        hendelse1.addKontekst(vedtaksperiode1)
        hendelse1.info("info message")
        hendelse1.warn("warn message")
        hendelse1.error("error message")
        val hendelse2 = TestHendelse(
            "Hendelse2",
            aktivitetslogg.barn(),
        )
        hendelse2.addKontekst(person)
        val arbeidsgiver2 =
            TestKontekst("Arbeidsgiver 2")
        hendelse2.addKontekst(arbeidsgiver2)
        val vedtaksperiode2 =
            TestKontekst("Vedtaksperiode 2")
        hendelse2.addKontekst(vedtaksperiode2)
        hendelse2.info("info message")
        hendelse2.error("error message")
        assertEquals(5, aktivitetslogg.aktivitetsteller())
        assertEquals(3, aktivitetslogg.logg(vedtaksperiode1).aktivitetsteller())
        assertEquals(2, aktivitetslogg.logg(arbeidsgiver2).aktivitetsteller())
    }

    @Test
    fun `Behov kan ha detaljer`() {
        val hendelse1 = TestHendelse(
            "Hendelse1",
            aktivitetslogg.barn(),
        )
        hendelse1.addKontekst(person)
        val param1 = "value"
        val param2 = LocalDate.now()
        hendelse1.behov(
            Aktivitetslogg.Aktivitet.Behov.Behovtype.personopplysninger,
            "Trenger personopplysninger",
            mapOf(
                "param1" to param1,
                "param2" to param2,
            ),
        )

        assertEquals(1, aktivitetslogg.behov().size)
        assertEquals(1, aktivitetslogg.behov().first().alleKonteksterAsMap().size)
        assertEquals(2, aktivitetslogg.behov().first().detaljer().size)
        assertEquals("Søker", aktivitetslogg.behov().first().alleKonteksterAsMap()["Søker"])
        assertEquals(param1, aktivitetslogg.behov().first().detaljer()["param1"])
        assertEquals(param2, aktivitetslogg.behov().first().detaljer()["param2"])
    }

    @Test
    fun `Behov med flere kontekster og detaljer skal bli mappet riktig`() {
        val hendelse1 = TestHendelse(
            "Hendelse1",
            aktivitetslogg.barn(),
        )
        hendelse1.addKontekst(person)
        hendelse1.addKontekst(TestKontekst("whatever"))
        val param1 = "value"
        val param2 = LocalDate.of(2022, 10, 1)
        hendelse1.behov(
            Aktivitetslogg.Aktivitet.Behov.Behovtype.personopplysninger,
            "Trenger personopplysninger",
            mapOf(
                "param1" to param1,
                "param2" to param2,
            ),
        )

        assertEquals(1, aktivitetslogg.behov().size)
        assertEquals(2, aktivitetslogg.behov().first().alleKonteksterAsMap().size)
        assertEquals(2, aktivitetslogg.behov().first().detaljer().size)
        assertEquals("Søker", aktivitetslogg.behov().first().alleKonteksterAsMap()["Søker"])
        assertEquals("whatever", aktivitetslogg.behov().last().alleKonteksterAsMap()["whatever"])
        assertEquals(param1, aktivitetslogg.behov().first().detaljer()["param1"])
        assertEquals(param2, aktivitetslogg.behov().first().detaljer()["param2"])
        assertAtBådeKontekstOgDetaljerBlirMappetInnIKafkaMeldingene(aktivitetslogg.behov())
    }

    // Denne metoden er lik-ish den som brukes i BehovMediator
    private fun assertAtBådeKontekstOgDetaljerBlirMappetInnIKafkaMeldingene(behov: List<Aktivitetslogg.Aktivitet.Behov>) {
        behov.groupBy { it.alleKonteksterAsMap() }.forEach { (kontekst, behov) ->
            val behovsliste = mutableListOf<String>()
            val id = UUID.randomUUID()

            mutableMapOf(
                "@event_name" to "behov",
                "@opprettet" to LocalDateTime.now(),
                "@id" to id,
                "@behov" to behovsliste,
            )
                .apply {
                    putAll(kontekst)
                    behov.forEach { behov ->
                        require(behov.type.name !in behovsliste) { "Kan ikke produsere samme behov ${behov.type.name} på samme kontekst" }
                        require(
                            behov.detaljer().filterKeys { this.containsKey(it) && this[it] != behov.detaljer()[it] }
                                .isEmpty(),
                        ) { "Kan ikke produsere behov med duplikate detaljer" }
                        behovsliste.add(behov.type.name)
                        putAll(behov.detaljer())
                    }
                }
                .also {
                    assertEquals("behov", it["@event_name"])
                    assertEquals(listOf("personopplysninger"), it["@behov"])
                    assertEquals("Søker", it["Søker"])
                    assertEquals("whatever", it["whatever"])
                    assertEquals("value", it["param1"])
                    assertEquals(LocalDate.of(2022, 10, 1), it["param2"])
                }
        }
    }

    private fun assertInfo(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : IAktivitetsloggVisitor {
                override fun visitInfo(
                    kontekster: List<Kontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Info,
                    melding: String,
                    tidsstempel: LocalDateTime,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private fun assertWarn(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : IAktivitetsloggVisitor {
                override fun visitWarn(
                    kontekster: List<Kontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Warn,
                    melding: String,
                    tidsstempel: LocalDateTime,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private fun assertError(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : IAktivitetsloggVisitor {
                override fun visitError(
                    kontekster: List<Kontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Error,
                    melding: String,
                    tidsstempel: LocalDateTime,
                ) {
                    visitorCalled = true
                    assertTrue(message in aktivitet.toString(), aktivitetslogg.toString())
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private fun assertSevere(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : IAktivitetsloggVisitor {
                override fun visitSevere(
                    kontekster: List<Kontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Severe,
                    melding: String,
                    tidsstempel: LocalDateTime,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private class TestKontekst(
        private val melding: String,
    ) : KontekstLogable {
        override fun opprettKontekst() = Kontekst(
            kontekstType = melding,
            kontekstMap = mapOf(melding to melding),
        )
    }

    private class TestHendelse(
        private val melding: String,
        val logg: Aktivitetslogg,
    ) : KontekstLogable, IAktivitetslogg by logg {
        init {
            logg.addKontekst(this)
        }

        override fun opprettKontekst() = Kontekst(kontekstType = "TestHendelse")
        override fun addKontekst(kontekst: KontekstLogable) {
            logg.addKontekst(kontekst)
        }
    }
}
