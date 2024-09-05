package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakServiceImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo

internal open class FørstegangsbehandlingContext(
    val behandlingService: BehandlingServiceImpl,
    val kvpVilkårService: KvpVilkårServiceImpl,
    val livsoppholdVilkårService: LivsoppholdVilkårServiceImpl,
    val rammevedtakService: RammevedtakServiceImpl,
    val rammevedtakRepo: RammevedtakPostgresRepo,
    val behandlingRepo: BehandlingPostgresRepo,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
            personopplysningRepo: PersonopplysningerRepo,
            meldekortRepo: MeldekortRepo,
            sakRepo: SakRepo,
            statistikkSakRepo: StatistikkSakRepo,
            statistikkStønadRepo: StatistikkStønadRepo,
        ): FørstegangsbehandlingContext {
            val behandlingRepo =
                BehandlingPostgresRepo(
                    sessionFactory = sessionFactory,
                )
            val rammevedtakRepo = RammevedtakPostgresRepo(
                sessionFactory = sessionFactory,
            )

            val rammevedtakService = RammevedtakServiceImpl(rammevedtakRepo)

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = behandlingRepo,
                    vedtakRepo = rammevedtakRepo,
                    personopplysningRepo = personopplysningRepo,
                    meldekortRepo = meldekortRepo,
                    sakRepo = sakRepo,
                    sessionFactory = sessionFactory,
                    statistikkSakRepo = statistikkSakRepo,
                    statistikkStønadRepo = statistikkStønadRepo,
                )

            val kvpVilkårService =
                KvpVilkårServiceImpl(
                    behandlingService = behandlingService,
                    behandlingRepo = behandlingRepo,
                )
            val livsoppholdVilkårService =
                LivsoppholdVilkårServiceImpl(
                    behandlingService = behandlingService,
                    behandlingRepo = behandlingRepo,
                )

            return FørstegangsbehandlingContext(
                behandlingService = behandlingService,
                kvpVilkårService = kvpVilkårService,
                livsoppholdVilkårService = livsoppholdVilkårService,
                rammevedtakService = rammevedtakService,
                rammevedtakRepo = rammevedtakRepo,
                behandlingRepo = behandlingRepo,
            )
        }
    }
}
