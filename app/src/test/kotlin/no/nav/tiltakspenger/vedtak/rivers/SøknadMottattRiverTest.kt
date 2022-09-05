package no.nav.tiltakspenger.vedtak.rivers

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.InMemorySøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SøknadMottattRiverTest {

    private companion object {
        val IDENT = "04927799109"
    }

    private val søkerRepository = InMemorySøkerRepository()
    private val testRapid = TestRapid()

    init {
        SøknadMottattRiver(
            rapidsConnection = testRapid,
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Skal generere behov og legge på kafka `() {
        testRapid.sendTestMessage(søknadMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("personopplysninger", field(0, "@behov")[0].asText())
            // De følgende feiler hvis feltet ikke er satt
            field(0, "@id").asText()
            field(0, "ident").asText()
            field(0, "@behovId").asText()
            assertEquals("SøkerRegistrertType", field(0, "tilstandtype").asText())
            assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    private fun søknadMottattEvent(): String =
        """
        {
          "@event_name": "søknad_mottatt",
          "søknad": {
            "id": "13306",
            "fornavn": "LEVENDE",
            "etternavn": "POTET",
            "ident": "04927799109",
            "deltarKvp": false,
            "deltarIntroduksjonsprogrammet": false,
            "oppholdInstitusjon": false,
            "typeInstitusjon": null,
            "tiltaksArrangoer": "foo",
            "tiltaksType": "JOBSOK",
            "opprettet": "2022-06-29T16:24:02.608",
            "brukerRegistrertStartDato": "2022-06-21",
            "brukerRegistrertSluttDato": "2022-06-30",
            "systemRegistrertStartDato": null,
            "systemRegistrertSluttDato": null,
            "barnetillegg": []
          },
          "@id": "369bf01c-f46f-4cb9-ba0d-01beb0905edc",
          "@opprettet": "2022-06-29T16:25:33.598375671",
          "system_read_count": 1,
          "system_participating_services": [
            {
              "id": "369bf01c-f46f-4cb9-ba0d-01beb0905edc",
              "time": "2022-06-29T16:25:33.598375671",
              "service": "tiltakspenger-mottak",
              "instance": "tiltakspenger-mottak-6c65db7887-ffwcv",
              "image": "ghcr.io/navikt/tiltakspenger-mottak:2074ee7461ad748d7c99d26ee5b7374e0c7fd9f4"
            }
          ]
        }
        """.trimIndent()

    //Meldingen som blir publisert ser ut omtrent som dette:
    @Suppress
    private fun behovsmelding(): String =
        """{
            "@event_name" : "behov",
            "@opprettet" : "2022-06-27T11:36:31.346814",
            "@id": "ed2fc977-2188-4bac-be16-7226dba5b9ea", 
            "@behov" : [ "Journalpost "],
            "ident" : "124567",
            "tilstand" : "MottattType", 
            "system_read_count" : 0,
            "system_participating_services" : [ 
                { "id" : "ed2fc977-2188-4bac-be16-7226dba5b9ea", "time" : "2022-06-27T11:36:31.389679 "}
            ]
           }""".trimIndent()
}
