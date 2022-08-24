package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Personinfo
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArenaTiltakMottattRiverTest {

    private companion object {
        val IDENT = "04927799109"
    }

    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        ArenaTiltakMottattRiver(
            rapidsConnection = testRapid,
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Når ArenaTiltak får en løsning på skjerming, skal den sende en behovsmelding etter ytelser`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            søknad = Søknad(
                id = "",
                fornavn = null,
                etternavn = null,
                ident = IDENT,
                deltarKvp = false,
                deltarIntroduksjonsprogrammet = null,
                oppholdInstitusjon = null,
                typeInstitusjon = null,
                tiltaksArrangoer = null,
                tiltaksType = null,
                opprettet = null,
                brukerRegistrertStartDato = null,
                brukerRegistrertSluttDato = null,
                systemRegistrertStartDato = null,
                systemRegistrertSluttDato = null,
                barnetillegg = listOf(),
                innhentet = LocalDateTime.now(),
            )
        )
        val persondataMottatthendelse = PersondataMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            ident = IDENT,
            personinfo = Personinfo(
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
        val søker = Søker(IDENT)
        søker.håndter(søknadMottatthendelse)
        søker.håndter(persondataMottatthendelse)
        søker.håndter(skjermingMottattHendelse)

        every { søkerRepository.hent(IDENT) } returns søker
        testRapid.sendTestMessage(arenaTiltakMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("arenaytelser", field(0, "@behov")[0].asText())
//            assertEquals("SøkerRegistrertType", field(0, "tilstandtype").asText())
            assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    private fun arenaTiltakMottattEvent(): String =
        """
           {
             "@behov": [
               "arenatiltak"
             ],
             "@id": "test",
             "@behovId": "behovId",
             "ident": "$IDENT",
             "testmelding": true,
             "@opprettet": "2022-08-22T14:59:46.491437009",
             "system_read_count": 0,
             "system_participating_services": [
               {
                 "id": "test",
                 "time": "2022-08-22T14:59:46.491437009",
                 "service": "tiltakspenger-arena",
                 "instance": "tiltakspenger-arena-75c76566d6-hpsh9",
                 "image": "ghcr.io/navikt/tiltakspenger-arena:63854e822f84b676f721c95a37dccc2105b0ef57"
               }
             ],
             "@løsning": {
               "arenatiltak": [
                 {
                   "tiltaksnavn": "ARBTREN",
                   "aktivitetId": "TA6734563",
                   "tiltakLokaltNavn": "Arbeidstrening",
                   "arrangoer": "STENDI SENIOR AS",
                   "bedriftsnummer": "986164189",
                   "deltakelsePeriode": {
                     "fom": "2022-07-04",
                     "tom": "2022-08-31"
                   },
                   "deltakelseProsent": 100,
                   "deltakerStatus": {
                     "statusNavn": "Gjennomføres",
                     "status": "GJENN"
                   },
                   "statusSistEndret": "2022-08-09",
                   "begrunnelseInnsoeking": "Trenger tiltaksplass",
                   "antallDagerPerUke": null
                 }
               ]
             }
           }
        """.trimIndent()
}

// eksempel påmelding som blir postet
//"""{
//    "@event_name": "behov",
//    "@opprettet": "2022-08-19T10:23:24.587525",
//    "@id": "735d324b-b6ec-498f-982d-38761caef06f",
//    "@behov": [
//    "arenaytelser"
//    ],
//    "ident": "04927799109",
//    "tilstandtype": "AvventerYtelser",
//    "system_read_count": 0,
//    "system_participating_services": [
//    {
//        "id": "735d324b-b6ec-498f-982d-38761caef06f",
//        "time": "2022-08-19T10:23:24.590127"
//    }
//    ]
//}"""
