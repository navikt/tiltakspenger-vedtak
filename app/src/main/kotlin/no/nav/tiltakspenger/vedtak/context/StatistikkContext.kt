package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadPostgresRepo

open class StatistikkContext(
    sessionFactory: SessionFactory,
) {
    open val statistikkSakRepo: StatistikkSakRepo by lazy { StatistikkSakRepoImpl(sessionFactory as PostgresSessionFactory) }
    open val statistikkStønadRepo: StatistikkStønadRepo by lazy { StatistikkStønadPostgresRepo(sessionFactory as PostgresSessionFactory) }
}
