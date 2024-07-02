package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.PleiepengerNærståendeSaksopplysning

data class PleiepengerNærståendeDelVilkår(
    val saksbehandlerSaksopplysning: PleiepengerNærståendeSaksopplysning.Saksbehandler?,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning? = saksbehandlerSaksopplysning
    override fun harSøknad(): Boolean = false

    companion object {
        operator fun invoke(vurderingsperiode: Periode) =
            PleiepengerNærståendeDelVilkår(null, vurderingsperiode, Periodisering(Utfall2.UAVKLART, vurderingsperiode))
    }

    init {
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.totalePeriode == vurderingsperiode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) må være lik vurderingsperioden $vurderingsperiode."
            }
        }
    }

    fun leggTilSaksopplysning(saksopplysning: PleiepengerNærståendeSaksopplysning.Saksbehandler): PleiepengerNærståendeDelVilkår {
        return this.copy(
            saksbehandlerSaksopplysning = saksopplysning,
        )
    }
}
