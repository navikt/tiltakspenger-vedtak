package no.nav.tiltakspenger.vedtak.repository.utbetaling

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.opprettUtbetalingsvedtak
import no.nav.tiltakspenger.vedtak.db.persisterRammevedtakMedUtfyltMeldekort
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UtbetalingsvedtakRepoImplTest {

    @Test
    fun `kan lagre og hente`() {
        withMigratedDb(runIsolated = true) { testDataHelper ->
            val (sak, meldekort) = testDataHelper.persisterRammevedtakMedUtfyltMeldekort()
            val utbetalingsvedtakRepo = testDataHelper.utbetalingsvedtakRepo
            val utbetalingsvedtak = meldekort.opprettUtbetalingsvedtak(sak.rammevedtak!!, null)
            utbetalingsvedtakRepo.lagre(utbetalingsvedtak)
            utbetalingsvedtakRepo.hentForVedtakId(utbetalingsvedtak.id) shouldBe utbetalingsvedtak
            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe listOf(utbetalingsvedtak)
            utbetalingsvedtakRepo.hentForFørstegangsbehandlingId(sak.førstegangsbehandling.id) shouldBe
                listOf(
                    utbetalingsvedtak,
                )
            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe listOf(utbetalingsvedtak)
            utbetalingsvedtakRepo.markerSendtTilUtbetaling(
                vedtakId = utbetalingsvedtak.id,
                tidspunkt = LocalDateTime.now(),
                utbetalingsrespons = SendtUtbetaling("myReq", "myRes", 202),
            )
            utbetalingsvedtakRepo.hentUtbetalingJsonForVedtakId(utbetalingsvedtak.id) shouldBe "myReq"

            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe emptyList()

            val oppdatertMedUtbetalingsdata = utbetalingsvedtakRepo.hentForVedtakId(utbetalingsvedtak.id)!!
            utbetalingsvedtakRepo.hentDeSomSkalJournalføres() shouldBe listOf(oppdatertMedUtbetalingsdata)
            utbetalingsvedtakRepo.markerJournalført(
                vedtakId = utbetalingsvedtak.id,
                journalpostId = JournalpostId("123"),
                tidspunkt = LocalDateTime.now(),
            )
            utbetalingsvedtakRepo.hentDeSomSkalJournalføres() shouldBe emptyList()
        }
    }
}
