package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

interface VilkårsvurderingKategori {
    fun lovreferanse(): Lovreferanse
    fun samletUtfall(): Utfall
    fun vurderinger(): List<Vurdering>
}
