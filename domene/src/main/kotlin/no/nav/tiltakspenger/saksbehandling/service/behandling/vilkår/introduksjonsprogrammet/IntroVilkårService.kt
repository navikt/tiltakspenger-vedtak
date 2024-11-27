package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.introduksjonsprogrammet

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand

interface IntroVilkårService {
    suspend fun leggTilSaksopplysning(command: LeggTilIntroSaksopplysningCommand): Either<KanIkkeLeggeTilSaksopplysning, Behandling>
}
