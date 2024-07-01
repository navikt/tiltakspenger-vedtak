package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

interface LivsoppholdDelVilkaar {
    val utfall: Periodisering<Utfall2>
    val vurderingsperiode: Periode
    fun samletUtfall(): SamletUtfall = when {
        utfall.perioder().any { it.verdi == Utfall2.UAVKLART } -> SamletUtfall.UAVKLART
        utfall.perioder().all { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.OPPFYLT
        utfall.perioder().all { it.verdi == Utfall2.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
        utfall.perioder().any() { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
        else -> throw IllegalStateException("Ugyldig utfall")
    }
}

data class AAPDelVilkaar(
    val saksbehandlerSaksopplysning: AAPSaksopplysning?,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkaar {

    companion object {
        operator fun invoke(vurderingsperiode: Periode) =
            AAPDelVilkaar(null, vurderingsperiode, Periodisering(Utfall2.UAVKLART, vurderingsperiode))
    }

    init {
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.totalePeriode == vurderingsperiode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) må være lik vurderingsperioden $vurderingsperiode."
            }
        }
    }

    fun leggTilSaksopplysning(saksopplysning: AAPSaksopplysning): AAPDelVilkaar {
        return this.copy(
            saksbehandlerSaksopplysning = saksopplysning,
        )
    }
}

data class AlderspensjonDelVilkaar(
    val søknadSaksopplysning: AlderspensjonSaksopplysning,
    val saksbehandlerSaksopplysning: AlderspensjonSaksopplysning?,
    val avklartSaksopplysning: AlderspensjonSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkaar {

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: AlderspensjonSaksopplysning.Søknad,
        ) = AlderspensjonDelVilkaar(
            søknadSaksopplysning,
            null,
            søknadSaksopplysning,
            vurderingsperiode,
            søknadSaksopplysning.vurderMaskinelt(),
        )
    }

    init {
        // TODO: Denne må utvides
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.totalePeriode == vurderingsperiode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) må være lik vurderingsperioden $vurderingsperiode."
            }
        }
    }

    fun leggTilSaksopplysning(saksopplysning: AlderspensjonSaksopplysning): AlderspensjonDelVilkaar {
        return when (saksopplysning) {
            is AlderspensjonSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is AlderspensjonSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
