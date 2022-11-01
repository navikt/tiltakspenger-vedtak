package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class InstitusjonVilkårsvurderingKategori(
    val institusjonsoppholdVilkårsvurdering: InstitusjonsoppholdVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD

    override fun samletUtfall(): Utfall =
        institusjonsoppholdVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        institusjonsoppholdVilkårsvurdering.vurderinger()
}
