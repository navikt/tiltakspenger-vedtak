package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilKravfristSaksopplysning(
    command: LeggTilKravfristSaksopplysningCommand,
): Førstegangsbehandling {
    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterKravdato(command),
    )
    return oppdatertFørstegangsbehandling
}
