package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand

interface LivsoppholdVilkårService {
    fun leggTilSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): Førstegangsbehandling
}
