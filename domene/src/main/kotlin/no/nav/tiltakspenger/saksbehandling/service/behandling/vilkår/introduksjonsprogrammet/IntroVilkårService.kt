package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand

interface IntroVilkårService {
    fun leggTilSaksopplysning(command: LeggTilIntroSaksopplysningCommand): Førstegangsbehandling
}
