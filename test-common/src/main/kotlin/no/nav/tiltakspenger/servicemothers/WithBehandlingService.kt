package no.nav.tiltakspenger.servicemothers

import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl

fun TestApplicationContext.withBehandlingService(block: (BehandlingService) -> Unit) {
    block(
        BehandlingServiceImpl(
            førstegangsbehandlingRepo = this.førstegangsbehandlingContext.behandlingRepo,
            rammevedtakRepo = this.førstegangsbehandlingContext.rammevedtakRepo,
            personopplysningRepo = this.personContext.personopplysningerRepo,
            meldekortRepo = this.meldekortContext.meldekortRepo,
            sakRepo = this.sakContext.sakRepo,
            sessionFactory = this.sessionFactory,
            statistikkSakRepo = this.statistikkContext.statistikkSakRepo,
            statistikkStønadRepo = this.statistikkContext.statistikkStønadRepo,
        ),
    )
}
