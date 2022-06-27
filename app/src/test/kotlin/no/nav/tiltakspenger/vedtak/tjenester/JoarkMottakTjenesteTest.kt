package no.nav.tiltakspenger.vedtak.tjenester

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JoarkMottakTjenesteTest {

    private companion object {
        val IDENT = "124567"
    }

    private val søkerRepositoryMock = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        JoarkMottakTjeneste(
            rapidsConnection = testRapid,
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepositoryMock,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `Skal hente saf post og legge på kafka `() {
        every { søkerRepositoryMock.hent(IDENT) } returns null
        testRapid.sendTestMessage(journalpostEvent())
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("Journalpost", field(0, "@behov")[0].asText())
            assertEquals("MottattType", field(0, "tilstand").asText())
            assertEquals("124567", field(0, "ident").asText())
        }
    }

    private fun journalpostEvent(): String =
        """{
          "@event_name": "event",
          "@id": "${UUID.randomUUID()}",
          "@event": [ "journalpost" ],
          "@opprettet" : "${LocalDateTime.now()}",
          "ident": "$IDENT"
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