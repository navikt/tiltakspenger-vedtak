package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.SøknadService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.vedtak.repository.benk.SaksoversiktPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakPostgresRepo

internal open class SakContext(
    val sakService: SakService,
    val sakRepo: SakRepo,
    val saksoversiktRepo: SaksoversiktRepo,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
            personGateway: PersonGateway,
            søknadService: SøknadService,
            skjermingGateway: SkjermingGateway,
            statistikkSakRepo: StatistikkSakRepo,
            tiltakGateway: TiltakGateway,
        ): SakContext {
            val saksoversiktRepo = SaksoversiktPostgresRepo(
                sessionFactory = sessionFactory,
            )
            val sakRepo = SakPostgresRepo(
                sessionFactory = sessionFactory,
            )
            val sakService = SakServiceImpl(
                sakRepo = sakRepo,
                søknadService = søknadService,
                personGateway = personGateway,
                skjermingGateway = skjermingGateway,
                tiltakGateway = tiltakGateway,
                sessionFactory = sessionFactory,
                statistikkSakRepo = statistikkSakRepo,
                saksoversiktRepo = saksoversiktRepo,
            )
            return SakContext(
                sakService = sakService,
                sakRepo = sakRepo,
                saksoversiktRepo = saksoversiktRepo,
            )
        }
    }
}
