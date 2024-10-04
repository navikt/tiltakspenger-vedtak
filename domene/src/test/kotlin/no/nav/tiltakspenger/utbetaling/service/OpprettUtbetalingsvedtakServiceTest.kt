package no.nav.tiltakspenger.utbetaling.service

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.fakes.repos.UtbetalingsvedtakFakeRepo
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.meldekortIverksatt
import no.nav.tiltakspenger.objectmothers.tilSendMeldekortTilBeslutterKommando
import kotlin.test.Test

internal class OpprettUtbetalingsvedtakServiceTest {

    @Test
    fun `neste utbetalingsvedtak peker på forrige`() {
        with(TestApplicationContext()) {
            val sak = this.meldekortIverksatt()
            val sakId = sak.id
            meldekortContext.sendMeldekortTilBeslutterService.sendMeldekortTilBeslutter(
                (sak.meldeperioder[1] as Meldekort.IkkeUtfyltMeldekort).tilSendMeldekortTilBeslutterKommando(ObjectMother.saksbehandler()),
            )
            meldekortContext.iverksettMeldekortService.iverksettMeldekort(
                IverksettMeldekortKommando(
                    meldekortId = sak.meldeperioder[1].id,
                    sakId = sakId,
                    beslutter = ObjectMother.beslutter(),
                ),
            )
            (utbetalingContext.utbetalingsvedtakRepo as UtbetalingsvedtakFakeRepo).hentForSakId(sakId).let {
                it.size shouldBe 2
                it[0].forrigeUtbetalingsvedtakId shouldBe null
                it[1].forrigeUtbetalingsvedtakId shouldBe it[0].id
            }
        }
    }
}
