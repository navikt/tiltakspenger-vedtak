package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.TiltakspengerVilkårsvurderingKategori


class Inngangsvilkårsvurderinger(
    val tiltakspengerYtelser: TiltakspengerVilkårsvurderingKategori,
    val statligeYtelser: StatligeYtelserVilkårsvurderingKategori,
    val kommunaleYtelser: KommunaleYtelserVilkårsvurderingKategori,
    val pensjonsordninger: PensjonsinntektVilkårsvurderingKategori,
    val lønnsinntekt: LønnsinntektVilkårsvurderingKategori,
    val institusjonopphold: InstitusjonVilkårsvurderingKategori
) {
    fun samletUtfall(): Utfall {
        val utfall =
            listOf(
                tiltakspengerYtelser.samletUtfall(),
                statligeYtelser.samletUtfall(),
                kommunaleYtelser.samletUtfall(),
                pensjonsordninger.samletUtfall(),
                lønnsinntekt.samletUtfall(),
                institusjonopphold.samletUtfall(),
            )
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    fun vurderinger(): List<Vurdering> =
        listOf(
            tiltakspengerYtelser.vurderinger(),
            statligeYtelser.vurderinger(),
            kommunaleYtelser.vurderinger(),
            pensjonsordninger.vurderinger(),
            lønnsinntekt.vurderinger(),
            institusjonopphold.vurderinger(),
        ).flatten()
}

fun List<Vurdering>.ikkeOppfylte() = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
