package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.alder

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand

interface AlderVilkårService {
    suspend fun leggTilSaksopplysning(command: LeggTilAlderSaksopplysningCommand): Either<KanIkkeLeggeTilSaksopplysning, Behandling>
}
