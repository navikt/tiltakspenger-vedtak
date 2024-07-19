package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.alder

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand

interface AlderVilkårService {
    fun leggTilSaksopplysning(command: LeggTilAlderSaksopplysningCommand): Førstegangsbehandling
}
