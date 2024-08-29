package no.nav.tiltakspenger.utbetaling.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.utbetaling.domene.tilStatistikk
import no.nav.tiltakspenger.utbetaling.domene.tilUtbetalingsperiode
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

/**
 * Forventet å kalles fra en jobb.
 * Henter alle godkjente meldekort, beregner og lagrer en utbetaling.
 * Grunnen til at vi ønsker å gjøre dette i et eget steg er at vi må kunne resende utbetaling til helved dersom noe feiler og da bør vi prøve den samme behandlingId på nytt, som de deduper på.
 */
class OpprettUtbetalingsvedtakService(
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val rammevedtakRepo: RammevedtakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
) {
    val logger = KotlinLogging.logger { }

    suspend fun opprettUtbetalingsvedtak(correlationId: CorrelationId) {
        utbetalingsvedtakRepo.hentGodkjenteMeldekortUtenUtbetalingsvedtak().forEach { meldekort: UtfyltMeldekort ->
            // Vi ønsker ikke at trøblete meldekort skal spenne ben for andre meldekort.
            Either
                .catch {
                    val rammevedtak: Rammevedtak = rammevedtakRepo.hent(meldekort.rammevedtakId)!!
                    require(rammevedtak.sakId == meldekort.sakId)
                    // TODO jah: Lag en type som passer på at disse er sortert riktig.
                    val eksisterendeUtbetalingsvedtak = utbetalingsvedtakRepo.hentForSakId(meldekort.sakId)
                    meldekort.tilUtbetalingsperiode(rammevedtak, eksisterendeUtbetalingsvedtak.lastOrNull()?.id).also {
                        utbetalingsvedtakRepo.lagre(it)
                        // TODO jah lager en herlig transaksjon som lagrer statistikk og utbetalingsvedtak i samme transaksjon.
                        statistikkStønadRepo.lagre(it.tilStatistikk())
                    }
                }.onLeft {
                    logger.error(it) { "Feil ved opprettelse av utbetalingsvedtak for meldekortId=${meldekort.id}" }
                }
        }
    }
}
