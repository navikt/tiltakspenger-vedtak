package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilTiltakSaksopplysning(
    command: LeggTilTiltakSaksopplysningCommand,
): Førstegangsbehandling {
    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterTiltak(command),
    )
    return oppdatertFørstegangsbehandling
}
