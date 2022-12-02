package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.TiltakspengerVilkårsvurdering

class TiltakspengerVilkårsvurderingKategori(val tiltakspengerVilkårsvurdering: TiltakspengerVilkårsvurdering) :
    VilkårsvurderingKategori {

    override fun vilkår(): Vilkår = Vilkår.TILTAKSPENGER

    override fun samletUtfall(): Utfall =
        tiltakspengerVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        tiltakspengerVilkårsvurdering.vurderinger()
}