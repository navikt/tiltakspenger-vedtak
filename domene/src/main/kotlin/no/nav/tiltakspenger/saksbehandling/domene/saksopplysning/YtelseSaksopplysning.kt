package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.dekkerHele
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDateTime

data class YtelseSaksopplysningerForEnKilde(
    val kilde: Kilde, // Hvorfor har vi kilde her? Det ligger jo inne i 'saksopplysninger'-lista?
    val periode: Periode, // Hvorfor har vi periode? Dette er vel vurderingsperioden, og den har man 'ett hakk ut'
    val saksopplysninger: List<YtelseSaksopplysning>,
    val tidspunkt: LocalDateTime, // Er dette tidspunktet saksopplysingen ble lagt til?
) {
    init {
        // Hvorfor skal disse sjekkene bare kjøres om det er saksbehandler-fakta?
        // Kan ikke andre kilder ha hull/overlapp osv?
        if (kilde == Kilde.SAKSB) {
            require(saksopplysninger.map { it.periode }.erInnenfor(periode)) { "Saksopplysninger kan ikke ha periode som er utenfor vurderingsperioden" }

            require(!saksopplysninger.map { it.periode }.inneholderOverlapp()) { "Saksopplysninger kan ikke overlappe" }

            require(saksopplysninger.map { it.periode }.dekkerHele(periode)) { "Saksopplysninger kan ikke ha hull" }

            // require(har bare samme kilde)
        }
    }

    fun erSatt() = saksopplysninger.isNotEmpty()
}

data class YtelseSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val periode: Periode,
    val harYtelse: Boolean,
) : SaksopplysningInterface {
    companion object {
        val YTELSESVILKÅR = listOf(
            Vilkår.AAP,
            Vilkår.DAGPENGER,
            Vilkår.TILTAKSPENGER,
        )
    }
}

fun List<SaksopplysningInterface>.harEttUniktVilkår(): Boolean = this.all { it.vilkår == this.first().vilkår }

fun List<YtelseSaksopplysning>.vilkårsvurder(vurderingsperiode: Periode): List<Vurdering> {
    check(this.harEttUniktVilkår()) { "Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår" }

    check(!this.map { it.periode }.inneholderOverlapp()) {
        "Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder"
    }

    check(this.map { it.periode }.dekkerHele(vurderingsperiode)) {
        "Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret"
    }

    check(this.map { it.periode }.erInnenfor(vurderingsperiode)) {
        "Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden"
    }

    return this.map {
        Vurdering(
            vilkår = it.vilkår,
            kilde = it.kilde,
            fom = it.periode.fra,
            tom = it.periode.til,
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
