package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden

fun Førstegangsbehandling.leggTilLivsoppholdSaksopplysning(
    command: LeggTilLivsoppholdSaksopplysningCommand,
): Either<PeriodenMåVæreLikVurderingsperioden, Førstegangsbehandling> {
    require(saksbehandler == command.saksbehandler.navIdent) {
        "Kan bare legge til saksopplysninger på egen sak. Saksbehandler på behandling: $saksbehandler, utførendeSaksbehandler: ${command.saksbehandler}, behandlingId: ${command.behandlingId}"
    }

    return vilkårssett
        .oppdaterLivsopphold(command)
        .map {
            this.copy(
                vilkårssett = it,
                saksbehandler = command.saksbehandler.navIdent,
            )
        }
}
