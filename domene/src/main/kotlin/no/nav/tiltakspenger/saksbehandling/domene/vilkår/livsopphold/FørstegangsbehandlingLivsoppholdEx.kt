package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden

fun Førstegangsbehandling.leggTilLivsoppholdSaksopplysning(
    command: LeggTilLivsoppholdSaksopplysningCommand,
): Either<PeriodenMåVæreLikVurderingsperioden, Førstegangsbehandling> {
    return vilkårssett.oppdaterLivsopphold(command).map { this.copy(vilkårssett = it) }
}
