package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand

interface LivsoppholdVilkårService {
    suspend fun leggTilSaksopplysning(
        command: LeggTilLivsoppholdSaksopplysningCommand,
    ): Either<KanIkkeLeggeTilSaksopplysning, Behandling>
}
