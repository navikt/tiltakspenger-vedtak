package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AlderspensjonSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType

data class AlderspensjonDelVilkår(
    val søknadSaksopplysning: AlderspensjonSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: AlderspensjonSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: AlderspensjonSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.ALDERSPENSJON
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning =
        saksbehandlerSaksopplysning ?: søknadSaksopplysning

    override fun harSøknad(): Boolean = true

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: AlderspensjonSaksopplysning.Søknad,
        ) = AlderspensjonDelVilkår(
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

    fun leggTilSaksopplysning(saksopplysning: AlderspensjonSaksopplysning): AlderspensjonDelVilkår {
        return when (saksopplysning) {
            is AlderspensjonSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is AlderspensjonSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
