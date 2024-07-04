package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand

interface KvpVilkårService {
    fun leggTilSaksopplysning(command: LeggTilKvpSaksopplysningCommand): Førstegangsbehandling
}
