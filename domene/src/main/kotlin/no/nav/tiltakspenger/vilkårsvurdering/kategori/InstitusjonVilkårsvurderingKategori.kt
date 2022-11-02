package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering

class InstitusjonVilkårsvurderingKategori(
    val institusjonsoppholdVilkårsvurdering: InstitusjonsoppholdVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.INSTITUSJONSOPPHOLD

    override fun samletUtfall(): Utfall =
        institusjonsoppholdVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        institusjonsoppholdVilkårsvurdering.vurderinger()
}
