package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.nyPersonopplysningHendelse
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class ArenaTiltakMottattRiverTest {

    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        ArenaTiltakMottattRiver(
            rapidsConnection = testRapid,
            innsendingMediator = InnsendingMediator(
                innsendingRepository = innsendingRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Når ArenaTiltak får en løsning på skjerming, skal den sende en behovsmelding etter ytelser`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            søknad = Søknad(
                søknadId = "42",
                journalpostId = "43",
                dokumentInfoId = "44",
                fornavn = null,
                etternavn = null,
                ident = IDENT,
                deltarKvp = false,
                deltarIntroduksjonsprogrammet = false,
                introduksjonsprogrammetDetaljer = null,
                oppholdInstitusjon = null,
                typeInstitusjon = null,
                opprettet = null,
                barnetillegg = emptyList(),
                tidsstempelHosOss = LocalDateTime.now(),
                tiltak = Tiltak.ArenaTiltak(
                    arenaId = "123",
                    arrangoernavn = "Tiltaksarrangør AS",
                    harSluttdatoFraArena = false,
                    tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                    erIEndreStatus = false,
                    opprinneligSluttdato = LocalDate.now(),
                    opprinneligStartdato = LocalDate.now(),
                    sluttdato = LocalDate.now(),
                    startdato = LocalDate.now()
                ),
                trygdOgPensjon = emptyList(),
                fritekst = null,
                vedlegg = emptyList(),
            )
        )
        val personopplysningerMottatthendelse = nyPersonopplysningHendelse(journalpostId = JOURNALPOSTID)

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            skjerming = Skjerming(
                ident = IDENT,
                skjerming = false,
                innhentet = LocalDateTime.now()
            )
        )
        val innsending = Innsending(JOURNALPOSTID)
        innsending.håndter(søknadMottatthendelse)
        innsending.håndter(personopplysningerMottatthendelse)
        innsending.håndter(skjermingMottattHendelse)

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsending
        testRapid.sendTestMessage(arenaTiltakMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("arenaytelser", field(0, "@behov")[0].asText())
//            assertEquals("SøkerRegistrertType", field(0, "tilstandtype").asText())
            assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    @Test
    fun `Når vi får en løsning på ArenaTiltak med feil skal vi ikke sende noen melding`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            søknad = nySøknadMedArenaTiltak(
                journalpostId = JOURNALPOSTID,
                ident = IDENT,
            )
        )

        val personopplysningerMottatthendelse = nyPersonopplysningHendelse(journalpostId = IDENT)

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            skjerming = Skjerming(
                ident = IDENT,
                skjerming = false,
                innhentet = LocalDateTime.now()
            )
        )
        val innsending = Innsending(JOURNALPOSTID)
        innsending.håndter(søknadMottatthendelse)
        innsending.håndter(personopplysningerMottatthendelse)
        innsending.håndter(skjermingMottattHendelse)

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsending
        testRapid.sendTestMessage(arenaTiltakMottattMedFeilEvent())
        with(testRapid.inspektør) {
            assertEquals(0, size)
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
             "journalpostId": "$JOURNALPOSTID",
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
                "arenatiltak": {
                  "feil": null,
                  "tiltaksaktiviteter": [
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
                        "innerText": "GJENN"
                      },
                      "statusSistEndret": "2022-08-09",
                      "begrunnelseInnsoeking": "Trenger tiltaksplass",
                      "antallDagerPerUke": null
                    }
                  ]
                }
              }
           }
        """.trimIndent()

    private fun arenaTiltakMottattMedFeilEvent(): String =
        """
           {
             "@behov": [
               "arenatiltak"
             ],
             "@id": "test",
             "@behovId": "behovId",
             "ident": "$IDENT",
             "journalpostId": "$JOURNALPOSTID",
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
                "arenatiltak": {
                  "feil": "PersonIkkeFunnet",
                  "tiltaksaktiviteter": null
                }
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
