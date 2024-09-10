package no.nav.tiltakspenger.vedtak.repository.utbetaling

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.tilUtbetalingsperiode
import no.nav.tiltakspenger.vedtak.db.persisterRammevedtakMedUtfyltMeldekort
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

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
            utbetalingsvedtakRepo.markerSendtTilUtbetaling(utbetalingsvedtak.id, SendtUtbetaling("", ""))
            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk() shouldBe emptyList()
        }
    }
}
