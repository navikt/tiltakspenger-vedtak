package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.dekkerHele
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class YtelseSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val fom: LocalDate,
    val tom: LocalDate,
    val harYtelse: Boolean,
) : SaksopplysningInterface

fun List<SaksopplysningInterface>.harEttUniktVilkår(): Boolean = this.all { it.vilkår == this.first().vilkår }

fun List<YtelseSaksopplysning>.vilkårsvurder(vurderingsperiode: Periode): List<Vurdering> {
    check(this.harEttUniktVilkår()) { "Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår" }

    check(!this.map { Periode(it.fom, it.tom) }.inneholderOverlapp()) {
        "Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder"
    }

    check(this.map { Periode(it.fom, it.tom) }.dekkerHele(vurderingsperiode)) {
        "Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret"
    }

    check(this.map { Periode(it.fom, it.tom) }.erInnenfor(vurderingsperiode)) {
        "Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden"
    }

    return this.map {
        Vurdering(
            vilkår = it.vilkår,
            kilde = it.kilde,
            fom = it.fom,
            tom = it.tom,
            utfall = if (it.harYtelse) {
                if (it.vilkår in listOf(
                        Vilkår.AAP,
                        Vilkår.DAGPENGER,
                        Vilkår.TILTAKSPENGER,
                    )
                ) {
                    Utfall.KREVER_MANUELL_VURDERING
                } else {
                    Utfall.IKKE_OPPFYLT
                }
            } else {
                Utfall.OPPFYLT
            },
            detaljer = it.detaljer,
        )
    }
}
