package no.nav.tiltakspenger.saksbehandling.domene.behandling

import arrow.core.Either
import arrow.core.right
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.nå
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.stønadsdagerTidslinje
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.vilkårssettTidslinje
import no.nav.tiltakspenger.saksbehandling.service.sak.KanIkkeStarteRevurdering
import java.lang.IllegalArgumentException

fun Sak.startRevurdering(
    kommando: StartRevurderingKommando,
): Either<KanIkkeStarteRevurdering, Pair<Sak, Behandling>> {
    val vilkårssett = this.vilkårssettTidslinje()
        ?: throw IllegalArgumentException("Må finnes et rammevedtak før man kan starte revurdering. sakId: ${this.id}")
    val stønadsdager = this.stønadsdagerTidslinje()
        ?: throw IllegalArgumentException("Må finnes et rammevedtak før man kan starte revurdering. sakId: ${this.id}")
    val opprettet = nå()
    val behandling = Behandling(
        id = BehandlingId.random(),
        sakId = this.id,
        saksnummer = this.saksnummer,
        fnr = this.fnr,
        vurderingsperiode = kommando.periode,
        søknad = null,
        saksbehandler = kommando.saksbehandler.navIdent,
        sendtTilBeslutning = null,
        beslutter = null,
        vilkårssett = vilkårssett,
        stønadsdager = stønadsdager,
        status = Behandlingsstatus.UNDER_BEHANDLING,
        attesteringer = emptyList(),
        opprettet = opprettet,
        iverksattTidspunkt = null,
        sendtTilDatadeling = null,
        sistEndret = opprettet,
        behandlingstype = Behandlingstype.REVURDERING,
    )
    return Pair(leggTilRevurdering(behandling), behandling).right()
}

fun Sak.leggTilRevurdering(
    behandling: Behandling,
): Sak {
    val behandlinger = this.behandlinger + behandling
    return this.copy(behandlinger = behandlinger)
}
