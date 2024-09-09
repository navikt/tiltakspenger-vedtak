package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadPostgresRepo

internal open class StatistikkContext(
    val statistikkSakRepo: StatistikkSakRepo,
    val statistikkStønadRepo: StatistikkStønadPostgresRepo,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
        ): StatistikkContext {
            val statistikkSakRepo = StatistikkSakRepoImpl(sessionFactory)
            val statistikkStønadRepo = StatistikkStønadPostgresRepo(sessionFactory)
            return StatistikkContext(
                statistikkSakRepo = statistikkSakRepo,
                statistikkStønadRepo = statistikkStønadRepo,
            )
        }
    }
}
