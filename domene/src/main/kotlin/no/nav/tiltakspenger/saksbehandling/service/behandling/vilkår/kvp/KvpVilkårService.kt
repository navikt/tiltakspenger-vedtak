package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand

interface KvpVilkårService {
    fun leggTilSaksopplysning(command: LeggTilKvpSaksopplysningCommand): KVPVilkår
    fun hent(behandlingId: BehandlingId): KVPVilkår
}
