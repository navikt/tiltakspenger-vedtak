package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SupplerendeStønadAlderSaksopplysning

data class SupplerendestønadAlderDelVilkår(
    val søknadSaksopplysning: SupplerendeStønadAlderSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: SupplerendeStønadAlderSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: SupplerendeStønadAlderSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning =
        saksbehandlerSaksopplysning ?: søknadSaksopplysning

    override fun harSøknad(): Boolean = true

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: SupplerendeStønadAlderSaksopplysning.Søknad,
        ) = SupplerendestønadAlderDelVilkår(
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

    fun leggTilSaksopplysning(saksopplysning: SupplerendeStønadAlderSaksopplysning): SupplerendestønadAlderDelVilkår {
        return when (saksopplysning) {
            is SupplerendeStønadAlderSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is SupplerendeStønadAlderSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
