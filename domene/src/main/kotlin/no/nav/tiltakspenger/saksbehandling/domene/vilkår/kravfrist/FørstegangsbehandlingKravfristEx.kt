package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

fun Førstegangsbehandling.leggTilKravfristSaksopplysning(
    command: LeggTilKravfristSaksopplysningCommand,
): Førstegangsbehandling {
    // TODO Feriegave fra Kew: Mulig å legge ved begge saksbehandlerne her og muligens en id hvis det finnes på commanden
    require(saksbehandler == command.saksbehandler.navIdent) { "Kan bare legge til saksopplysninger på egen sak" }

    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterKravdato(command),
        saksbehandler = command.saksbehandler.navIdent,
    )
    return oppdatertFørstegangsbehandling
}
