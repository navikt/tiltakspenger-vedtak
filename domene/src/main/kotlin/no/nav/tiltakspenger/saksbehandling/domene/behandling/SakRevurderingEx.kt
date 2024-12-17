package no.nav.tiltakspenger.saksbehandling.domene.behandling

import arrow.core.Either
import arrow.core.right
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.nå
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.krympStønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.krympVilkårssett
import no.nav.tiltakspenger.saksbehandling.service.sak.KanIkkeStarteRevurdering

fun Sak.startRevurdering(
    kommando: StartRevurderingKommando,
): Either<KanIkkeStarteRevurdering, Pair<Sak, Behandling>> {
    // Merk at vi beholder eventuelle tidspunkt og IDer.
    val vilkårssett = this.krympVilkårssett(kommando.periode).single()
    val stønadsdager = this.krympStønadsdager(kommando.periode).single()
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
        vilkårssett = vilkårssett.verdi,
        stønadsdager = stønadsdager.verdi,
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
    return copy(behandlinger = behandlinger.leggTilRevurdering(behandling))
}
