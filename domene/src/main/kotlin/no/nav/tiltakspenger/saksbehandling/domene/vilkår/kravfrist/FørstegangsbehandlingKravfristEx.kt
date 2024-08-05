package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

/** Brukes ikke i MVPen. */
fun Førstegangsbehandling.leggTilKravfristSaksopplysning(
    command: LeggTilKravfristSaksopplysningCommand,
): Førstegangsbehandling {
    require(saksbehandler == command.saksbehandler.navIdent) { "Kan bare legge til saksopplysninger på egen sak. Saksbehandler på behandling: $saksbehandler, utførendeSaksbehandler: ${command.saksbehandler}, behandlingId: ${command.behandlingId}" }

    val oppdatertFørstegangsbehandling = this.copy(
        vilkårssett = vilkårssett.oppdaterKravdato(command),
        saksbehandler = command.saksbehandler.navIdent,
    )
    return oppdatertFørstegangsbehandling
}
