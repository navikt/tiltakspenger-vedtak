package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class ArenaYtelseMottattRiverTest {

    private companion object {
        const val IDENT = "04927799109"
    }

    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        ArenaYtelserMottattRiver(
            rapidsConnection = testRapid,
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Når ArenaYtelser får en løsning på tiltak, skal den ikke sende noen nye behov`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            søknad = Søknad(
                søknadId = "42",
                journalpostId = "43",
                dokumentInfoId = "44",
                fornavn = null,
                etternavn = null,
                ident = IDENT,
                deltarKvp = false,
                deltarIntroduksjonsprogrammet = null,
                oppholdInstitusjon = null,
                typeInstitusjon = null,
                opprettet = null,
                barnetillegg = emptyList(),
                tidsstempelHosOss = LocalDateTime.now(),
                tiltak = Tiltak.ArenaTiltak(),
                trygdOgPensjon = emptyList(),
                fritekst = null
            )
        )
        val personopplysningerMottatthendelse = PersonopplysningerMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            personopplysninger = Personopplysninger(
                ident = "",
                fødselsdato = LocalDate.MAX,
                fornavn = "",
                mellomnavn = null,
                etternavn = "",
                fortrolig = false,
                strengtFortrolig = false,
                innhentet = LocalDateTime.now(),
            )
        )

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            skjerming = Skjerming(
                ident = IDENT,
                skjerming = false,
                innhentet = LocalDateTime.now()
            )
        )

        val tiltakMottattHendelse = ArenaTiltakMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            tiltaksaktivitet = listOf(
                Tiltaksaktivitet(
                    tiltak = Tiltaksaktivitet.Tiltak.ARBRRHDAG,
                    aktivitetId = "",
                    tiltakLokaltNavn = null,
                    arrangoer = null,
                    bedriftsnummer = null,
                    deltakelsePeriode = null,
                    deltakelseProsent = null,
                    deltakerStatus = Tiltaksaktivitet.DeltakerStatus.GJENN,
                    statusSistEndret = null,
                    begrunnelseInnsoeking = "",
                    antallDagerPerUke = null,
                    innhentet = LocalDateTime.now()
                )
            )
        )
        val søker = Søker(IDENT)
        søker.håndter(søknadMottatthendelse)
        søker.håndter(personopplysningerMottatthendelse)
        søker.håndter(skjermingMottattHendelse)
        søker.håndter(tiltakMottattHendelse)

        every { søkerRepository.hent(IDENT) } returns søker
        testRapid.sendTestMessage(ytelserMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(0, size)
        }
    }

    private fun ytelserMottattEvent(): String =
        """
           {
             "@behov": [
               "arenaytelser"
             ],
             "@id": "test",
             "@behovId": "behovId",
             "ident": "$IDENT",
             "fom": "2019-10-01",
             "tom": "2022-06-01",
             "testmelding": true,
             "@opprettet": "2022-08-22T15:56:35.310409409",
             "system_read_count": 0,
             "system_participating_services": [
               {
                 "id": "test",
                 "time": "2022-08-22T15:56:35.310409409",
                 "service": "tiltakspenger-arena",
                 "instance": "tiltakspenger-arena-75c76566d6-hpsh9",
                 "image": "ghcr.io/navikt/tiltakspenger-arena:63854e822f84b676f721c95a37dccc2105b0ef57"
               }
             ],
             "@løsning": {
               "arenaytelser": []
             }
           }
        """.trimIndent()
}
