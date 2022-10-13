package no.nav.tiltakspenger.vedtak.rivers

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.nyPersonopplysningHendelse
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class SkjermingMottattRiverTest {


    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
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
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
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
            ident = ident,
            søknad = Søknad(
                søknadId = "42",
                journalpostId = "43",
                dokumentInfoId = "44",
                fornavn = null,
                etternavn = null,
                ident = ident,
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
            )
        )
        val personopplysningerMottattHendelse = nyPersonopplysningHendelse(ident = ident)
        val søker = Søker(ident)
        every { søkerRepository.hent(ident) } returns søker

        // when
        søker.håndter(mottattSøknadHendelse)
        søker.håndter(personopplysningerMottattHendelse)
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
