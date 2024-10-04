package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.domene.KanIkkeIverksetteMeldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.utbetaling.domene.opprettUtbetalingsvedtak
import no.nav.tiltakspenger.utbetaling.domene.tilStatistikk
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

class IverksettMeldekortService(
    val sakService: SakService,
    val meldekortRepo: MeldekortRepo,
    val sessionFactory: SessionFactory,
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
) {
    suspend fun iverksettMeldekort(
        kommando: IverksettMeldekortKommando,
    ): Either<KanIkkeIverksetteMeldekort, Meldekort.UtfyltMeldekort> {
        val meldekortId = kommando.meldekortId
        val sakId = kommando.sakId
        val sak = sakService.hentForSakId(sakId, kommando.beslutter, correlationId = kommando.correlationId)
            ?: throw IllegalArgumentException("Fant ikke sak med id $sakId")
        val meldekort: Meldekort = sak.hentMeldekort(meldekortId)
            ?: throw IllegalArgumentException("Fant ikke meldekort med id $meldekortId i sak $sakId")
        meldekort as Meldekort.UtfyltMeldekort
        require(meldekort.beslutter == null) {
            "Meldekort $meldekortId er allerede iverksatt"
        }

        val rammevedtak = sak.rammevedtak
            ?: throw IllegalArgumentException("Fant ikke rammevedtak for sak $sakId")

        return meldekort.iverksettMeldekort(kommando.beslutter).onRight { iverksattMeldekort ->
            val nesteMeldekort = iverksattMeldekort.opprettNesteMeldekort(rammevedtak.utfallsperioder)
            val eksisterendeUtbetalingsvedtak = sak.utbetalinger
            val utbetalingsvedtak =
                iverksattMeldekort.opprettUtbetalingsvedtak(rammevedtak, eksisterendeUtbetalingsvedtak.lastOrNull()?.id)
            val utbetalingsstatistikk = utbetalingsvedtak.tilStatistikk()
            sessionFactory.withTransactionContext { tx ->
                meldekortRepo.oppdater(iverksattMeldekort, tx)
                nesteMeldekort.onRight { meldekortRepo.lagre(it, tx) }
                utbetalingsvedtakRepo.lagre(utbetalingsvedtak, tx)
                statistikkStønadRepo.lagre(utbetalingsstatistikk, tx)
            }
        }
    }
}
