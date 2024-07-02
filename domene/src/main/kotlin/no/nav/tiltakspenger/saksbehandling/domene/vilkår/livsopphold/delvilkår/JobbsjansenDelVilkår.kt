package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.JobbsjansenSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType

data class JobbsjansenDelVilkår(
    val søknadSaksopplysning: JobbsjansenSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: JobbsjansenSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: JobbsjansenSaksopplysning,
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<Utfall2>,
) : LivsoppholdDelVilkår {

    override val livsoppholdytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.JOBBSJANSEN
    override fun avklartSaksopplysning(): LivsoppholdSaksopplysning =
        saksbehandlerSaksopplysning ?: søknadSaksopplysning

    override fun harSøknad(): Boolean = true

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            søknadSaksopplysning: JobbsjansenSaksopplysning.Søknad,
        ) = JobbsjansenDelVilkår(
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

    fun leggTilSaksopplysning(saksopplysning: JobbsjansenSaksopplysning): JobbsjansenDelVilkår {
        return when (saksopplysning) {
            is JobbsjansenSaksopplysning.Saksbehandler -> this.copy(saksbehandlerSaksopplysning = saksopplysning)
            is JobbsjansenSaksopplysning.Søknad -> this.copy(søknadSaksopplysning = saksopplysning)
        }
    }
}
