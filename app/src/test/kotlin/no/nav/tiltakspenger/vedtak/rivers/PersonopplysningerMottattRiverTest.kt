package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.*
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.Month

internal class PersonopplysningerMottattRiverTest {
    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val mediatorSpy = spyk(SøkerMediator(søkerRepository = søkerRepository, rapidsConnection = testRapid))

    init {
        PersonopplysningerMottattRiver(
            rapidsConnection = testRapid,
            søkerMediator = mediatorSpy
        )
    }

    @Suppress("LongMethod")
    @Test
    fun `Når PersonopplysningerRiver får en løsning på person, skal den sende en behovsmelding etter skjerming`() {
        val ident = "04927799109"
        val personopplysningerMottattHendelse =
            File("src/test/resources/personopplysningerMottattHendelse.json").readText()
        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = ident,
            søknad = nySøknadMedArenaTiltak(
                ident = ident,
            )
        )
        val søker = Søker(ident)
        every { søkerRepository.hent(ident) } returns søker

        søker.håndter(søknadMottattHendelse)
        testRapid.sendTestMessage(personopplysningerMottattHendelse)

        assertEquals(SøkerTilstandType.AvventerSkjermingdata, søker.tilstand.type)
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("skjerming", field(0, "@behov")[0].asText())
            assertEquals(SøkerTilstandType.AvventerPersonopplysninger.name, field(0, "tilstandtype").asText())
            assertEquals(ident, field(0, "ident").asText())
            verify {
                mediatorSpy.håndter(
                    withArg<PersonopplysningerMottattHendelse> {
                        assertEquals(ident, it.ident())
                        val søkerMedPersonoppl =
                            it.personopplysninger().filterIsInstance<Personopplysninger.Søker>().first()
                        assertEquals(ident, søkerMedPersonoppl.ident)
                        assertEquals(LocalDate.of(1983, Month.JULY, 4), søkerMedPersonoppl.fødselsdato)
                        assertEquals("Knuslete", søkerMedPersonoppl.fornavn)
                        assertEquals("Melon", søkerMedPersonoppl.mellomnavn)
                        assertEquals("Ekspedisjon", søkerMedPersonoppl.etternavn)
                        assertEquals("Oslo", søkerMedPersonoppl.kommune)
                        assertEquals("460105", søkerMedPersonoppl.bydel)
                        assertFalse(søkerMedPersonoppl.fortrolig)
                        assertFalse(søkerMedPersonoppl.strengtFortrolig)
                        assertNull(søkerMedPersonoppl.skjermet)
                        val barnOpplysninger =
                            it.personopplysninger().filterIsInstance<Personopplysninger.BarnMedIdent>()
                        assertEquals(1, barnOpplysninger.size)
                        assertEquals("Fornem", barnOpplysninger.first().fornavn)
                        assertEquals("Jogger", barnOpplysninger.first().etternavn)
                        assertEquals("07085512345", barnOpplysninger.first().ident)
                        val barnUtenIden = it.personopplysninger().filterIsInstance<Personopplysninger.BarnUtenIdent>()
                        assertEquals(1, barnUtenIden.size)
                        assertEquals("Liten", barnUtenIden.first().fornavn)
                        assertEquals("Opal", barnUtenIden.first().etternavn)
                        assertEquals(LocalDate.of(2052, Month.JANUARY, 1), barnUtenIden.first().fødselsdato)
                    }
                )
            }
        }
    }
}
