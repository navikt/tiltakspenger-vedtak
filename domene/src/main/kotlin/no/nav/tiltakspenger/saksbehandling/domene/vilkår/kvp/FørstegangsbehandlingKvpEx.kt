package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilKvpSaksopplysning(
    command: LeggTilKvpSaksopplysningCommand,
): Førstegangsbehandling {
    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterKVP(command),
    )
    return oppdatertFørstegangsbehandling
}
