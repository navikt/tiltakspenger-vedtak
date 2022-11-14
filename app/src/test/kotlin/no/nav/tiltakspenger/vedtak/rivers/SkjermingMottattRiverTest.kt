package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.nyPersonopplysningHendelse
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SkjermingMottattRiverTest {


    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val ident = "05906398291"
    private val løsning = """
            {
              "@behov": [
                "skjerming"
              ],
              "@id": "test",
              "@behovId": "behovId",
              "ident": "05906398291",
              "fom": "2019-10-01",
              "tom": "2022-06-01",
              "@opprettet": "2022-08-19T12:28:01.422516717",
              "system_read_count": 0,
              "system_participating_services": [
                {
                  "id": "test",
                  "time": "2022-08-19T12:28:01.422516717",
                  "service": "tiltakspenger-skjerming",
                  "instance": "tiltakspenger-skjerming-69f669bc95-plxb6",
                  "image": "ghcr.io/navikt/tiltakspenger-skjerming:128cdcc92ea50224bbccdc4c565e3f408e093213"
                }
              ],
              "@løsning": {
                "skjerming": false
              }
            }
        """

    init {
        SkjermingMottattRiver(
            rapidsConnection = testRapid,
            innsendingMediator = InnsendingMediator(
                innsendingRepository = innsendingRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @Test
    fun `En løsning for skjerming mottas`() {
        // given
        val aktivitetslogg = Aktivitetslogg(forelder = null)
        val mottattSøknadHendelse = SøknadMottattHendelse(
            aktivitetslogg = aktivitetslogg,
            journalpostId = ident,
            søknad = nySøknadMedArenaTiltak(
                ident = ident,
            )
        )
        val personopplysningerMottattHendelse = nyPersonopplysningHendelse(journalpostId = ident)
        val innsending = Innsending(ident)
        every { innsendingRepository.hent(ident) } returns innsending

        // when
        innsending.håndter(mottattSøknadHendelse)
        innsending.håndter(personopplysningerMottattHendelse)
        testRapid.sendTestMessage(løsning)

        // then
        with(testRapid.inspektør) {
            assertEquals(1, this.size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals(ident, field(0, "ident").asText())
            assertEquals("arenatiltak", field(0, "@behov")[0].asText())
        }
    }
}
