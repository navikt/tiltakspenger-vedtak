package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår

interface LivsoppholdVilkårService {
    fun leggTilSaksopplysning(
        command: LeggTilLivsoppholdSaksopplysningCommand,
    ): Either<LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden, Førstegangsbehandling>
}
