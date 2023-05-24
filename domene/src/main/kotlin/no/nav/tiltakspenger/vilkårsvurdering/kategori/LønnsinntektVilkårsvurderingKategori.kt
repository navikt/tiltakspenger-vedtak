package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.EtterlønnVilkårsvurdering

class LønnsinntektVilkårsvurderingKategori(
    val etterlønnVilkårsvurdering: EtterlønnVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.LØNNSINNTEKT

    override fun samletUtfall(): Utfall =
        etterlønnVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        etterlønnVilkårsvurdering.vurderinger()
}
