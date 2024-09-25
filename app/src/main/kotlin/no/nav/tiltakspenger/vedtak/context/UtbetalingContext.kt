package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.utbetaling.client.iverksett.UtbetalingHttpClient
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.JournalførUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.OpprettUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.SendUtbetalingerService
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo

@Suppress("unused")
open class UtbetalingContext(
    sessionFactory: SessionFactory,
    rammevedtakRepo: RammevedtakRepo,
    statistikkStønadRepo: StatistikkStønadRepo,
    dokumentGateway: DokumentGateway,
) {
    private val tokenProviderUtbetaling = AzureTokenProvider(config = Configuration.oauthConfigUtbetaling())
    open val utbetalingGateway: UtbetalingGateway by lazy {
        UtbetalingHttpClient(
            endepunkt = Configuration.utbetalingClientConfig().baseUrl,
            getToken = tokenProviderUtbetaling::getToken,
        )
    }
    open val utbetalingsvedtakRepo: UtbetalingsvedtakRepo by lazy {
        UtbetalingsvedtakPostgresRepo(
            sessionFactory as PostgresSessionFactory,
        )
    }
    val opprettUtbetalingsvedtakService by lazy {
        OpprettUtbetalingsvedtakService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            rammevedtakRepo = rammevedtakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
            sessionFactory = sessionFactory,
        )
    }
    val hentUtbetalingsvedtakService by lazy {
        HentUtbetalingsvedtakService(utbetalingsvedtakRepo)
    }
    val sendUtbetalingerService: SendUtbetalingerService by lazy {
        SendUtbetalingerService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            utbetalingsklient = utbetalingGateway,
        )
    }
    val journalførUtbetalingsvedtakService by lazy {
        JournalførUtbetalingsvedtakService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            dokumentGateway = dokumentGateway,
        )
    }
}
