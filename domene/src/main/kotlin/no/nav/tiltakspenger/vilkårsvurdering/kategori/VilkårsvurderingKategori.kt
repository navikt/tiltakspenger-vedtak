package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

interface VilkårsvurderingKategori {
    fun vilkår(): Vilkår
    fun samletUtfall(): Utfall
    fun vurderinger(): List<Vurdering>
}
