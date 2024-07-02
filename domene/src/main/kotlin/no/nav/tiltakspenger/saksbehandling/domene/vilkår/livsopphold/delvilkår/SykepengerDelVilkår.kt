package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SykepengerSaksopplysning

data class SykepengerDelVilkår(
    val søknadSaksopplysning: SykepengerSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: SykepengerSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: SykepengerSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SYKEPENGER
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning =
        saksbehandlerSaksopplysning ?: søknadSaksopplysning

    override fun harSøknad(): Boolean = true

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: SykepengerSaksopplysning.Søknad,
        ) = SykepengerDelVilkår(
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

    fun leggTilSaksopplysning(saksopplysning: SykepengerSaksopplysning): SykepengerDelVilkår {
        return when (saksopplysning) {
            is SykepengerSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is SykepengerSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
