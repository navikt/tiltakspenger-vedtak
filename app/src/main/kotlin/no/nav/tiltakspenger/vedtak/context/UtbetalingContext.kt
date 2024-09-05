package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.utbetaling.client.iverksett.UtbetalingHttpClient
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.JournalførUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.OpprettUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.SendUtbetalingerService
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo

@Suppress("unused")
internal open class UtbetalingContext(
    val utbetalingGateway: UtbetalingGateway,
    val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    val opprettUtbetalingsvedtakService: OpprettUtbetalingsvedtakService,
    val hentUtbetalingsvedtakService: HentUtbetalingsvedtakService,
    val sendUtbetalingerService: SendUtbetalingerService,
    val journalførUtbetalingsvedtakService: JournalførUtbetalingsvedtakService,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
            rammevedtakRepo: RammevedtakPostgresRepo,
            statistikkStønadRepo: StatistikkStønadPostgresRepo,
            dokumentGateway: DokumentGateway,
        ): UtbetalingContext {
            val utbetalingsvedtakRepo =
                UtbetalingsvedtakPostgresRepo(
                    sessionFactory = sessionFactory,
                )
            val opprettUtbetalingsvedtakService =
                OpprettUtbetalingsvedtakService(
                    utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                    rammevedtakRepo = rammevedtakRepo,
                    statistikkStønadRepo = statistikkStønadRepo,
                )
            val tokenProviderUtbetaling = AzureTokenProvider(config = Configuration.oauthConfigUtbetaling())
            val utbetalingGateway: UtbetalingGateway =
                UtbetalingHttpClient(
                    endepunkt = Configuration.utbetalingClientConfig().baseUrl,
                    getToken = tokenProviderUtbetaling::getToken,
                )
            val hentUtbetalingsvedtakService =
                HentUtbetalingsvedtakService(
                    utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                )
            val sendUtbetalingerService =
                SendUtbetalingerService(
                    utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                    utbetalingsklient = utbetalingGateway,
                )
            val journalførMeldekortService = JournalførUtbetalingsvedtakService(
                utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                dokumentGateway = dokumentGateway,
            )
            return UtbetalingContext(
                utbetalingGateway = utbetalingGateway,
                utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                sendUtbetalingerService = sendUtbetalingerService,
                hentUtbetalingsvedtakService = hentUtbetalingsvedtakService,
                opprettUtbetalingsvedtakService = opprettUtbetalingsvedtakService,

                journalførUtbetalingsvedtakService = journalførMeldekortService,
            )
        }
    }
}
