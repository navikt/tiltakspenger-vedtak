package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PersonMottattRiverTest {

    private companion object {
        val IDENT = "04927799109"
    }

    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        PersondataMottattRiver(
            rapidsConnection = testRapid,
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Når PersonRiver får en løsning på person, skal den sende en behovsmelding etter skjerming`() {
        val hendelse = SøknadMottattHendelse(
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
        val søker = Søker(IDENT)
        søker.håndter(hendelse)

        every { søkerRepository.hent(IDENT) } returns søker
        testRapid.sendTestMessage(personMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("Skjermingdata", field(0, "@behov")[0].asText())
//            assertEquals("SøkerRegistrertType", field(0, "tilstandtype").asText())
            assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    private fun personMottattEvent(): String =
        """
           {
             "@behov": [
               "persondata"
             ],
             "@id": "test",
             "@behovId": "behovId",
             "ident": "$IDENT",
             "@opprettet": "2022-08-18T17:44:24.723046748",
             "system_read_count": 0,
             "system_participating_services": [
               {
                 "id": "test",
                 "time": "2022-08-18T17:44:24.723046748",
                 "service": "tiltakspenger-fakta-person",
                 "instance": "tiltakspenger-fakta-person-56bffc459b-4rm9s",
                 "image": "ghcr.io/navikt/tiltakspenger-fakta-person:fea0180a813ea2a49c4200906cdb844ada797109"
               }
             ],
             "@løsning": {
               "persondata": {
                 "person": {
                   "fødselsdato": "1983-07-04",
                   "fornavn": "Knuslete",
                   "mellomnavn": null,
                   "etternavn": "Ekspedisjon",
                   "adressebeskyttelseGradering": null,
                   "gtKommune": null,
                   "gtBydel": "460105",
                   "gtLand": null,
                   "barn": []
                 },
                 "feil": null
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
//    "Skjermingdata"
//    ],
//    "ident": "04927799109",
//    "tilstandtype": "AvventerPersondataType",
//    "system_read_count": 0,
//    "system_participating_services": [
//    {
//        "id": "735d324b-b6ec-498f-982d-38761caef06f",
//        "time": "2022-08-19T10:23:24.590127"
//    }
//    ]
//}"""