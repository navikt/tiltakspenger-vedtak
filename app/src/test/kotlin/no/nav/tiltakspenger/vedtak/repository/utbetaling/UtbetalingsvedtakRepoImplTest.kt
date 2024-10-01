package no.nav.tiltakspenger.vedtak.repository.utbetaling

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.tilUtbetalingsperiode
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
            val utbetalingsvedtak = meldekort.tilUtbetalingsperiode(sak.vedtak.single(), null)
            utbetalingsvedtakRepo.hentGodkjenteMeldekortUtenUtbetalingsvedtak() shouldBe listOf(meldekort)
            utbetalingsvedtakRepo.lagre(utbetalingsvedtak)
            utbetalingsvedtakRepo.hentForVedtakId(utbetalingsvedtak.id) shouldBe utbetalingsvedtak
            utbetalingsvedtakRepo.hentForSakId(sak.id) shouldBe listOf(utbetalingsvedtak)
            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe listOf(utbetalingsvedtak)
            utbetalingsvedtakRepo.hentForFørstegangsbehandlingId(sak.førstegangsbehandling.id) shouldBe
                listOf(
                    utbetalingsvedtak,
                )
            utbetalingsvedtakRepo.hentGodkjenteMeldekortUtenUtbetalingsvedtak() shouldBe emptyList()

            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe listOf(utbetalingsvedtak)
            utbetalingsvedtakRepo.markerSendtTilUtbetaling(
                vedtakId = utbetalingsvedtak.id,
                tidspunkt = LocalDateTime.now(),
                utbetalingsrespons = SendtUtbetaling("myReq", "myRes"),
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
