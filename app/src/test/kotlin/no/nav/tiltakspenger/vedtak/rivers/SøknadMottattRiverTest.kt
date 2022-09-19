package no.nav.tiltakspenger.vedtak.rivers

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.søker.InMemorySøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SøknadMottattRiverTest {

    private companion object {
        const val IDENT = "04927799109"
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
    fun `Skal generere behov med arenatiltak og legge på kafka `() {
        testRapid.sendTestMessage(søknadMottattArenatiltakEvent())
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

    @Test
    fun `Skal generere behov med brukertiltak og legge på kafka `() {
        testRapid.sendTestMessage(søknadMottattBrukertiltakEvent())
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

    @Test
    fun `Skal generere behov med barnetillegg uten ident og legge på kafka `() {
        testRapid.sendTestMessage(søknadMottattBarnUtenIdent())
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

    @Test
    fun `Skal generere behov med barnetillegg med ident og legge på kafka `() {
        testRapid.sendTestMessage(søknadMottattBarnMedIdent())
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

    private fun søknadMottattArenatiltakEvent(): String =
        """
        {
          "@event_name": "søknad_mottatt",
          "søknad": {
            "søknadId": "whatever",
            "journalpostId": "whatever2",
            "dokumentInfoId": "whatever3",
            "id": "13306",
            "fornavn": "LEVENDE",
            "etternavn": "POTET",
            "ident": "$IDENT",
            "deltarKvp": false,
            "deltarIntroduksjonsprogrammet": false,
            "oppholdInstitusjon": false,
            "typeInstitusjon": null,
            "tiltaksArrangoer": "foo",
            "tiltaksType": "JOBSOK",
            "arenaTiltak" : {
                 "arenaId" : "id",
                 "arrangoer" : "navn",
                 "harSluttdatoFraArena" : false,
                 "tiltakskode" : "MENTOR",
                 "erIEndreStatus" : false,
                 "opprinneligSluttdato": null,
                 "opprinneligStartdato" : null,
                 "sluttdato" : null,
                 "startdato" : null
            },
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

    private fun søknadMottattBarnUtenIdent(): String =
        """
            {
              "@event_name": "søknad_mottatt",
              "søknad": {
                "søknadId": "13521",
                "journalpostId": "573780628",
                "dokumentInfoId": "599042454",
                "fornavn": "DYPSINDIG",
                "etternavn": "IDÉ",
                "ident": "$IDENT",
                "deltarKvp": true,
                "deltarIntroduksjonsprogrammet": false,
                "oppholdInstitusjon": true,
                "typeInstitusjon": "annet",
                "opprettet": "2022-09-19T10:25:51.568",
                "barnetillegg": [
                  {
                    "ident": null,
                    "fødselsdato": "2022-04-01",
                    "alder": 0,
                    "land": "NOR"
                  }
                ],
                "arenaTiltak": {
                  "arenaId": "138377101",
                  "arrangoer": "STENDI SENIOR AS",
                  "harSluttdatoFraArena": true,
                  "tiltakskode": "ARBTREN",
                  "erIEndreStatus": false,
                  "opprinneligSluttdato": "2022-08-31",
                  "opprinneligStartdato": "2022-07-04",
                  "sluttdato": "2022-08-31",
                  "startdato": "2022-07-04"
                },
                "brukerregistrertTiltak": null,
                "trygdOgPensjon": [
                  {
                    "utbetaler": "Test1 AS",
                    "prosent": 50,
                    "fom": "2022-09-01",
                    "tom": "2022-09-25"
                  },
                  {
                    "utbetaler": "Test2 AS",
                    "prosent": 30,
                    "fom": "2022-09-07",
                    "tom": "2022-09-23"
                  }
                ],
                "fritekst": "test"
              },
              "@id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
              "@opprettet": "2022-09-19T10:44:27.281195318",
              "system_read_count": 1,
              "system_participating_services": [
                {
                  "id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
                  "time": "2022-09-19T10:44:27.281195318",
                  "service": "tiltakspenger-mottak",
                  "instance": "tiltakspenger-mottak-7cdcf6dbb9-6xlhj",
                  "image": "ghcr.io/navikt/tiltakspenger-mottak:711257b4c82c374dbbd768708e225fd10b2e7d6f"
                },
                {
                  "id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
                  "time": "2022-09-19T11:54:35.709771210",
                  "service": "tiltakspenger-vedtak",
                  "instance": "tiltakspenger-vedtak-769b84ff87-6m5ts",
                  "image": "ghcr.io/navikt/tiltakspenger-vedtak:263907dd2bdfdd4d9912fec6a721a7558a6111d7"
                }
              ]
            }
        """.trimIndent()

    private fun søknadMottattBarnMedIdent(): String =
        """
            {
              "@event_name": "søknad_mottatt",
              "søknad": {
                "søknadId": "13521",
                "journalpostId": "573780628",
                "dokumentInfoId": "599042454",
                "fornavn": "DYPSINDIG",
                "etternavn": "IDÉ",
                "ident": "$IDENT",
                "deltarKvp": true,
                "deltarIntroduksjonsprogrammet": false,
                "oppholdInstitusjon": true,
                "typeInstitusjon": "annet",
                "opprettet": "2022-09-19T10:25:51.568",
                "barnetillegg": [
                  {
                    "ident": "123",
                    "fødselsdato": null,
                    "alder": 0,
                    "land": "NOR"
                  }
                ],
                "arenaTiltak": {
                  "arenaId": "138377101",
                  "arrangoer": "STENDI SENIOR AS",
                  "harSluttdatoFraArena": true,
                  "tiltakskode": "ARBTREN",
                  "erIEndreStatus": false,
                  "opprinneligSluttdato": "2022-08-31",
                  "opprinneligStartdato": "2022-07-04",
                  "sluttdato": "2022-08-31",
                  "startdato": "2022-07-04"
                },
                "brukerregistrertTiltak": null,
                "trygdOgPensjon": [
                  {
                    "utbetaler": "Test1 AS",
                    "prosent": 50,
                    "fom": "2022-09-01",
                    "tom": "2022-09-25"
                  },
                  {
                    "utbetaler": "Test2 AS",
                    "prosent": 30,
                    "fom": "2022-09-07",
                    "tom": "2022-09-23"
                  }
                ],
                "fritekst": "test"
              },
              "@id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
              "@opprettet": "2022-09-19T10:44:27.281195318",
              "system_read_count": 1,
              "system_participating_services": [
                {
                  "id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
                  "time": "2022-09-19T10:44:27.281195318",
                  "service": "tiltakspenger-mottak",
                  "instance": "tiltakspenger-mottak-7cdcf6dbb9-6xlhj",
                  "image": "ghcr.io/navikt/tiltakspenger-mottak:711257b4c82c374dbbd768708e225fd10b2e7d6f"
                },
                {
                  "id": "0dbdb45b-62ac-45fe-9d47-b50aecb339c0",
                  "time": "2022-09-19T11:54:35.709771210",
                  "service": "tiltakspenger-vedtak",
                  "instance": "tiltakspenger-vedtak-769b84ff87-6m5ts",
                  "image": "ghcr.io/navikt/tiltakspenger-vedtak:263907dd2bdfdd4d9912fec6a721a7558a6111d7"
                }
              ]
            }
        """.trimIndent()
    private fun søknadMottattBrukertiltakEvent(): String =
        """
        {
          "@event_name": "søknad_mottatt",
          "søknad": {
            "søknadId": "whatever",
            "journalpostId": "whatever2",
            "dokumentInfoId": "whatever3",
            "id": "13306",
            "fornavn": "LEVENDE",
            "etternavn": "POTET",
            "ident": "$IDENT",
            "deltarKvp": false,
            "deltarIntroduksjonsprogrammet": false,
            "oppholdInstitusjon": false,
            "typeInstitusjon": null,
            "tiltaksArrangoer": "foo",
            "tiltaksType": "JOBSOK",
            "brukerregistrertTiltak": {
              "tiltakstype": "Annet",
              "arrangoernavn": "test as",
              "beskrivelse": "Intro",
              "fom": "2022-04-01",
              "tom": "2022-04-22",
              "adresse": "Storgata 1",
              "postnummer": "0318",
              "antallDager": 5
            },
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
