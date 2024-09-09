package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.felles.service.PersonService
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo

/**
 * Åpen så den kan overstyres i test
 */
@Suppress("unused")
internal open class AuditContext(
    val auditService: AuditService,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
        ): AuditContext {
            val meldekortRepo = MeldekortPostgresRepo(
                sessionFactory = sessionFactory,
            )

            val sakRepo = SakPostgresRepo(
                sessionFactory = sessionFactory,
            )

            val behandlingRepo =
                BehandlingPostgresRepo(
                    sessionFactory = sessionFactory,
                )

            val utbetalingsvedtakRepo =
                UtbetalingsvedtakPostgresRepo(
                    sessionFactory = sessionFactory,
                )

            val søknadRepo = PostgresSøknadRepo(sessionFactory = sessionFactory)

            val personService = PersonService(
                meldekortRepo = meldekortRepo,
                sakRepo = sakRepo,
                behandlingRepo = behandlingRepo,
                utbetalingsvedtakRepo = utbetalingsvedtakRepo,
                søknadRepo = søknadRepo,
            )

            return AuditContext(
                auditService = AuditService(personService = personService),
            )
        }
    }
}
