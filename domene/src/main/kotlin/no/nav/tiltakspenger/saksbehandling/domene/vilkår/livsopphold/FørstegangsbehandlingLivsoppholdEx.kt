package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilLivsoppholdSaksopplysning(
    command: LeggTilLivsoppholdSaksopplysningCommand,
): Førstegangsbehandling {
    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterLivsopphold(command),
    )
    return oppdatertFørstegangsbehandling
}
