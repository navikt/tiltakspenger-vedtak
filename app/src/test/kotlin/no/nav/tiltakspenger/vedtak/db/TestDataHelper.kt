package no.nav.tiltakspenger.vedtak.db

import no.nav.tiltakspenger.common.SaksnummerGenerator
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.vedtak.log
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.benk.SaksoversiktPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo
import javax.sql.DataSource

internal class TestDataHelper(
    val dataSource: DataSource,
    val saksnummerGenerator: SaksnummerGenerator,
) {
    private val sessionCounter = SessionCounter(log)
    val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)
    val personopplysningerRepo = PersonopplysningerPostgresRepo(sessionFactory)
    val søknadRepo = PostgresSøknadRepo(sessionFactory)
    val behandlingRepo = BehandlingPostgresRepo(sessionFactory)
    val vedtakRepo = RammevedtakPostgresRepo(sessionFactory)
    val sakRepo = SakPostgresRepo(sessionFactory)
    val saksoversiktRepo = SaksoversiktPostgresRepo(sessionFactory)
    val statistikkSakRepo = StatistikkSakRepoImpl(sessionFactory)
    val statistikkStønadRepo = StatistikkStønadPostgresRepo(sessionFactory)
    val meldekortRepo = MeldekortPostgresRepo(sessionFactory)
    val utbetalingsvedtakRepo: UtbetalingsvedtakRepo = UtbetalingsvedtakPostgresRepo(sessionFactory)
}

private val dbManager = TestDatabaseManager()

/**
 * @param runIsolated Tømmer databasen før denne testen for kjøre i isolasjon. Brukes når man gjør operasjoner på tvers av saker.
 */
internal fun withMigratedDb(runIsolated: Boolean = false, test: (TestDataHelper) -> Unit) {
    dbManager.withMigratedDb(runIsolated, test)
}
