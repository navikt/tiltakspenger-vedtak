package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.PensjonsinntektSaksopplysning

// TODO: Litt usikker på om dette er livsopphold, eller om den skal inn under lønn?
data class PensjonsinntektDelVilkår(
    val søknadSaksopplysning: PensjonsinntektSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: PensjonsinntektSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: PensjonsinntektSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PENSJONSINNTEKT
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning =
        saksbehandlerSaksopplysning ?: søknadSaksopplysning

    override fun harSøknad(): Boolean = true

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: PensjonsinntektSaksopplysning.Søknad,
        ) = PensjonsinntektDelVilkår(
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

    fun leggTilSaksopplysning(saksopplysning: PensjonsinntektSaksopplysning): PensjonsinntektDelVilkår {
        return when (saksopplysning) {
            is PensjonsinntektSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is PensjonsinntektSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
