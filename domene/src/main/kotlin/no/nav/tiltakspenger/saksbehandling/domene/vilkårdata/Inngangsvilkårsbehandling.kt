package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

interface Inngangsvilkårsbehandling {

    fun vilkår(): Inngangsvilkår
    fun vurdering(): Vurdering
}

interface LivsoppholdDelVilkårsbehandling {
    fun vilkår(): LivsoppholdDelVilkår
    fun vurdering(): Vurdering
}
