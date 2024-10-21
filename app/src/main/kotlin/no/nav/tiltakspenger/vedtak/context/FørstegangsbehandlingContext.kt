package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.distribusjon.ports.DokdistGateway
import no.nav.tiltakspenger.felles.NavIdentClient
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.JournalførVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.distribuering.DistribuerVedtaksbrevService
import no.nav.tiltakspenger.saksbehandling.service.journalføring.JournalførVedtaksbrevService
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakServiceImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo

open class FørstegangsbehandlingContext(
    sessionFactory: SessionFactory,
    meldekortRepo: MeldekortRepo,
    sakRepo: SakRepo,
    statistikkSakRepo: StatistikkSakRepo,
    statistikkStønadRepo: StatistikkStønadRepo,
    gitHash: String,
    journalførVedtaksbrevGateway: JournalførVedtaksbrevGateway,
    genererVedtaksbrevGateway: GenererVedtaksbrevGateway,
    tilgangsstyringService: TilgangsstyringService,
    personService: PersonService,
    dokdistGateway: DokdistGateway,
    navIdentClient: NavIdentClient,
) {
    open val rammevedtakRepo: RammevedtakRepo by lazy { RammevedtakPostgresRepo(sessionFactory as PostgresSessionFactory) }
    open val behandlingRepo: BehandlingRepo by lazy { BehandlingPostgresRepo(sessionFactory as PostgresSessionFactory) }
    val behandlingService: BehandlingService by lazy {
        BehandlingServiceImpl(
            førstegangsbehandlingRepo = behandlingRepo,
            rammevedtakRepo = rammevedtakRepo,
            meldekortRepo = meldekortRepo,
            sakRepo = sakRepo,
            sessionFactory = sessionFactory,
            statistikkSakRepo = statistikkSakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
            tilgangsstyringService = tilgangsstyringService,
            personService = personService,
            gitHash = gitHash,
        )
    }
    val kvpVilkårService: KvpVilkårService by lazy {
        KvpVilkårServiceImpl(
            behandlingService = behandlingService,
            behandlingRepo = behandlingRepo,
        )
    }
    val livsoppholdVilkårService: LivsoppholdVilkårService by lazy {
        LivsoppholdVilkårServiceImpl(
            behandlingService = behandlingService,
            behandlingRepo = behandlingRepo,
        )
    }
    val rammevedtakService: RammevedtakService by lazy { RammevedtakServiceImpl(rammevedtakRepo) }

    val journalførVedtaksbrevService by lazy {
        JournalførVedtaksbrevService(
            journalførVedtaksbrevGateway = journalførVedtaksbrevGateway,
            rammevedtakRepo = rammevedtakRepo,
            genererVedtaksbrevGateway = genererVedtaksbrevGateway,
            personService = personService,
            navIdentClient = navIdentClient,
        )
    }

    val distribuerVedtaksbrevService by lazy {
        DistribuerVedtaksbrevService(
            dokdistGateway = dokdistGateway,
            rammevedtakRepo = rammevedtakRepo,
        )
    }
}
