package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class LivsoppholdYtelseDelVilkår private constructor(
    val vurderingsperiode: Periode,
    val vilkår: LivsoppholdDelVilkår,
    val opprinneligYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
    val korrigertYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning?,
    val avklartYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
    val vurdering: Vurdering,
) {

    private fun faktaavklar(): LivsoppholdYtelseDelVilkår {
        return this.copy(
            avklartYtelseSaksopplysning = korrigertYtelseSaksopplysning
                ?: opprinneligYtelseSaksopplysning,
        )
    }

    private fun vilkårsvurder(): LivsoppholdYtelseDelVilkår {
        return this.copy(vurdering = vilkårsvurder(this.avklartYtelseSaksopplysning))
    }

    fun oppdaterSaksopplysning(ytelseSaksopplysning: LivsoppholdYtelseSaksopplysning): LivsoppholdYtelseDelVilkår {
        return if (ytelseSaksopplysning.kilde == Kilde.SAKSB) {
            this.copy(
                korrigertYtelseSaksopplysning = ytelseSaksopplysning,
            )
        } else {
            this.copy(
                opprinneligYtelseSaksopplysning = ytelseSaksopplysning,
            )
        }.faktaavklar().vilkårsvurder()
    }

    companion object {

        operator fun invoke(vurderingsperiode: Periode, vilkår: LivsoppholdDelVilkår): LivsoppholdYtelseDelVilkår {
            val tomYtelseSaksopplysning = LivsoppholdYtelseSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
            )
            return LivsoppholdYtelseDelVilkår(
                vurderingsperiode,
                vilkår,
                tomYtelseSaksopplysning,
                tomYtelseSaksopplysning,
                tomYtelseSaksopplysning,
                vilkårsvurder(tomYtelseSaksopplysning),
            )
        }

        fun fromDb(
            vurderingsperiode: Periode,
            vilkår: LivsoppholdDelVilkår,
            opprinneligYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
            korrigertYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning?,
            avklartYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
            vurdering: Vurdering,
        ): LivsoppholdYtelseDelVilkår =
            LivsoppholdYtelseDelVilkår(
                vurderingsperiode,
                vilkår,
                opprinneligYtelseSaksopplysning,
                korrigertYtelseSaksopplysning,
                avklartYtelseSaksopplysning,
                vurdering,
            )

        private fun vilkårsvurder(ytelseSaksopplysning: LivsoppholdYtelseSaksopplysning): Vurdering {
            if (ytelseSaksopplysning.vilkår in listOf(LivsoppholdDelVilkår.AAP, LivsoppholdDelVilkår.DAGPENGER) &&
                ytelseSaksopplysning.kilde != Kilde.SAKSB
            ) {
                return Vurdering(
                    Periodisering(
                        Utfall.UAVKLART,
                        ytelseSaksopplysning.harYtelse.totalePeriode,
                    ),
                    ytelseSaksopplysning.detaljer,
                )
            }

            return Vurdering(
                detaljer = ytelseSaksopplysning.detaljer,
                utfall = ytelseSaksopplysning.harYtelse.map {
                    when (it) {
                        HarYtelse.HAR_YTELSE -> Utfall.IKKE_OPPFYLT
                        HarYtelse.HAR_IKKE_YTELSE -> Utfall.OPPFYLT
                        else -> Utfall.UAVKLART
                    }
                },
            )
        }
    }
}
