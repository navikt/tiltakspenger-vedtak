package no.nav.tiltakspenger.servicemothers

import no.nav.tiltakspenger.fakes.clients.BrevPublisherGatewayFake
import no.nav.tiltakspenger.fakes.repos.BehandlingFakeRepo
import no.nav.tiltakspenger.fakes.repos.MeldekortFakeRepo
import no.nav.tiltakspenger.fakes.repos.PersonopplysningerFakeRepo
import no.nav.tiltakspenger.fakes.repos.RammevedtakFakeRepo
import no.nav.tiltakspenger.fakes.repos.SakFakeRepo
import no.nav.tiltakspenger.fakes.repos.SaksoversiktFakeRepo
import no.nav.tiltakspenger.fakes.repos.StatistikkSakFakeRepo
import no.nav.tiltakspenger.fakes.repos.StatistikkStønadFakeRepo
import no.nav.tiltakspenger.fakes.repos.SøknadFakeRepo
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.TestSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl

interface BehandlingServiceMother {

    fun withBehandlingService(
        eksisterendeSøknader: List<Søknad> = emptyList(),
        eksisterenPersonopplysninger: Map<SakId, SakPersonopplysninger> = emptyMap(),
        søknadRepo: SøknadFakeRepo = SøknadFakeRepo(eksisterendeSøknader),
        behandlingRepo: BehandlingFakeRepo = BehandlingFakeRepo(null),
        personopplysningerRepo: PersonopplysningerFakeRepo = PersonopplysningerFakeRepo(eksisterenPersonopplysninger),
        statistikkSakRepo: StatistikkSakFakeRepo = StatistikkSakFakeRepo(),
        statistikkStønadRepo: StatistikkStønadFakeRepo = StatistikkStønadFakeRepo(),
        saksoversiktRepo: SaksoversiktFakeRepo = SaksoversiktFakeRepo(
            søknadFakeRepo = søknadRepo,
            behandlingFakeRepo = behandlingRepo,
        ),
        vedtakRepo: RammevedtakFakeRepo = RammevedtakFakeRepo(),
        meldekortRepo: MeldekortFakeRepo = MeldekortFakeRepo(),
        sakRepo: SakFakeRepo = SakFakeRepo(
            personopplysningerRepo = personopplysningerRepo,
            behandlingRepo = behandlingRepo,
            vedtakRepo = vedtakRepo,
            meldekortRepo = meldekortRepo,
        ),
        brevPublisherGateway: BrevPublisherGatewayFake = BrevPublisherGatewayFake(),
        sessionFactory: TestSessionFactory = TestSessionFactory(),
        block: (BehandlingService, behandlingServiceFakes: BehandlingServiceFakes) -> Unit,
    ) {
        val (behandlingService, behandlingServiceFakes) = behandlingService(
            eksisterendeSøknader = eksisterendeSøknader,
            eksisterenPersonopplysninger = eksisterenPersonopplysninger,
            søknadRepo = søknadRepo,
            behandlingRepo = behandlingRepo,
            personopplysningerRepo = personopplysningerRepo,
            statistikkSakRepo = statistikkSakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
            saksoversiktRepo = saksoversiktRepo,
            vedtakRepo = vedtakRepo,
            sakRepo = sakRepo,
            brevPublisherGateway = brevPublisherGateway,
            meldekortRepo = meldekortRepo,
            sessionFactory = sessionFactory,
        )
        block(behandlingService, behandlingServiceFakes)
    }

    fun behandlingService(
        eksisterendeSøknader: List<Søknad> = emptyList(),
        eksisterenPersonopplysninger: Map<SakId, SakPersonopplysninger> = emptyMap(),
        søknadRepo: SøknadFakeRepo = SøknadFakeRepo(eksisterendeSøknader),
        behandlingRepo: BehandlingFakeRepo = BehandlingFakeRepo(null),
        personopplysningerRepo: PersonopplysningerFakeRepo = PersonopplysningerFakeRepo(eksisterenPersonopplysninger),
        statistikkSakRepo: StatistikkSakFakeRepo = StatistikkSakFakeRepo(),
        statistikkStønadRepo: StatistikkStønadFakeRepo = StatistikkStønadFakeRepo(),
        saksoversiktRepo: SaksoversiktFakeRepo = SaksoversiktFakeRepo(
            søknadFakeRepo = søknadRepo,
            behandlingFakeRepo = behandlingRepo,
        ),
        meldekortRepo: MeldekortFakeRepo = MeldekortFakeRepo(),
        vedtakRepo: RammevedtakFakeRepo = RammevedtakFakeRepo(),
        sakRepo: SakFakeRepo = SakFakeRepo(
            personopplysningerRepo = personopplysningerRepo,
            behandlingRepo = behandlingRepo,
            vedtakRepo = vedtakRepo,
            meldekortRepo = meldekortRepo,
        ),
        brevPublisherGateway: BrevPublisherGatewayFake = BrevPublisherGatewayFake(),
        sessionFactory: TestSessionFactory = TestSessionFactory(),
    ): Pair<BehandlingService, BehandlingServiceFakes> {
        return BehandlingServiceImpl(
            behandlingRepo = behandlingRepo,
            vedtakRepo = vedtakRepo,
            personopplysningRepo = personopplysningerRepo,
            meldekortRepo = meldekortRepo,
            sakRepo = sakRepo,
            sessionFactory = sessionFactory,
            statistikkSakRepo = statistikkSakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
        ) to BehandlingServiceFakes(
            behandlingRepo = behandlingRepo,
            vedtakRepo = vedtakRepo,
            personopplysningRepo = personopplysningerRepo,
            meldekortRepo = meldekortRepo,
            sakRepo = sakRepo,
            sessionFactory = sessionFactory,
            statistikkSakRepo = statistikkSakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
        )
    }
}

data class BehandlingServiceFakes(
    val behandlingRepo: BehandlingFakeRepo,
    val vedtakRepo: RammevedtakFakeRepo,
    val personopplysningRepo: PersonopplysningerFakeRepo,
    val meldekortRepo: MeldekortFakeRepo,
    val sakRepo: SakFakeRepo,
    val sessionFactory: TestSessionFactory,
    val statistikkSakRepo: StatistikkSakFakeRepo,
    val statistikkStønadRepo: StatistikkStønadFakeRepo,
)
