package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand

interface KvpVilkårService {
    suspend fun leggTilSaksopplysning(command: LeggTilKvpSaksopplysningCommand): Either<KanIkkeLeggeTilSaksopplysning, Behandling>
}
