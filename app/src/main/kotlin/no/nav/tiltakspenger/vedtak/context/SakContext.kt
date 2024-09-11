package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
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

open class SakContext(
    sessionFactory: SessionFactory,
    personGateway: PersonGateway,
    søknadService: SøknadService,
    skjermingGateway: SkjermingGateway,
    statistikkSakRepo: StatistikkSakRepo,
    tiltakGateway: TiltakGateway,
    gitHash: String,
) {
    val sakService: SakService by lazy {
        SakServiceImpl(
            sakRepo = sakRepo,
            søknadService = søknadService,
            personGateway = personGateway,
            skjermingGateway = skjermingGateway,
            tiltakGateway = tiltakGateway,
            sessionFactory = sessionFactory,
            statistikkSakRepo = statistikkSakRepo,
            saksoversiktRepo = saksoversiktRepo,
            gitHash = gitHash,
        )
    }
    open val sakRepo: SakRepo by lazy {
        SakPostgresRepo(
            sessionFactory = sessionFactory as PostgresSessionFactory,
        )
    }
    open val saksoversiktRepo: SaksoversiktRepo by lazy {
        SaksoversiktPostgresRepo(
            sessionFactory = sessionFactory as PostgresSessionFactory,
        )
    }
}