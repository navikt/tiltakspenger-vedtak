package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.OvergangsstønadSaksopplysning

data class OvergangsstønadDelVilkår(
    val saksbehandlerSaksopplysning: OvergangsstønadSaksopplysning.Saksbehandler?,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.OVERGANGSSTØNAD
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning? = saksbehandlerSaksopplysning
    override fun harSøknad(): Boolean = false

    companion object {
        operator fun invoke(vurderingsperiode: Periode) =
            OvergangsstønadDelVilkår(null, vurderingsperiode, Periodisering(Utfall2.UAVKLART, vurderingsperiode))
    }

    init {
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.totalePeriode == vurderingsperiode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) må være lik vurderingsperioden $vurderingsperiode."
            }
        }
    }

    fun leggTilSaksopplysning(saksopplysning: OvergangsstønadSaksopplysning.Saksbehandler): OvergangsstønadDelVilkår {
        return this.copy(
            saksbehandlerSaksopplysning = saksopplysning,
        )
    }
}
