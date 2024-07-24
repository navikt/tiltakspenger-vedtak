package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilAlderSaksopplysning(
    command: LeggTilKravdatoSaksopplysningCommand,
): Førstegangsbehandling {
    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterKravdato(command),
    )
    return oppdatertFørstegangsbehandling
}
