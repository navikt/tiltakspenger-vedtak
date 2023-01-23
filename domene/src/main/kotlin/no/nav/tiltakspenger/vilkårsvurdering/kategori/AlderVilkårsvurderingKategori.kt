package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderVilkårsvurdering

class AlderVilkårsvurderingKategori(
    val alderVilkårsvurdering: AlderVilkårsvurdering
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.ALDER

    override fun samletUtfall(): Utfall = alderVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> = alderVilkårsvurdering.vurderinger()
}
