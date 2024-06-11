package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.LivsoppholdSaksopplysningOgUtfallForPeriode

data class LivsoppholdYtelseVilkårData private constructor(
    val vurderingsperiode: Periode,
    val vilkår: LivsoppholdDelVilkår,
    val opprinneligYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
    val korrigertYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning?,
    val avklartYtelseSaksopplysning: LivsoppholdYtelseSaksopplysning,
    val vurdering: LivsoppholdDelVurdering,
) {

    private fun faktaavklar(): LivsoppholdYtelseVilkårData {
        return this.copy(
            avklartYtelseSaksopplysning = korrigertYtelseSaksopplysning
                ?: opprinneligYtelseSaksopplysning,
        )
    }

    private fun vilkårsvurder(): LivsoppholdYtelseVilkårData {
        return this.copy(vurdering = vilkårsvurder(this.avklartYtelseSaksopplysning))
    }

    fun oppdaterSaksopplysning(ytelseSaksopplysning: LivsoppholdYtelseSaksopplysning): LivsoppholdYtelseVilkårData {
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

    // TODO: Denne er ment å være midlertidig. Kanskje..?
    fun periodiseringAvSaksopplysningOgUtfall(): Periodisering<LivsoppholdSaksopplysningOgUtfallForPeriode> {
        return avklartYtelseSaksopplysning.harYtelse.kombiner(vurdering.utfall) { harYtelse, utfall ->
            LivsoppholdSaksopplysningOgUtfallForPeriode(
                avklartYtelseSaksopplysning.vilkår,
                avklartYtelseSaksopplysning.kilde,
                avklartYtelseSaksopplysning.detaljer,
                avklartYtelseSaksopplysning.saksbehandler,
                harYtelse,
                utfall,
            )
        }
    }

    companion object {

        operator fun invoke(vurderingsperiode: Periode, vilkår: LivsoppholdDelVilkår): LivsoppholdYtelseVilkårData {
            val tomYtelseSaksopplysning = LivsoppholdYtelseSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
            )
            return LivsoppholdYtelseVilkårData(
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
            vurdering: LivsoppholdDelVurdering,
        ): LivsoppholdYtelseVilkårData =
            LivsoppholdYtelseVilkårData(
                vurderingsperiode,
                vilkår,
                opprinneligYtelseSaksopplysning,
                korrigertYtelseSaksopplysning,
                avklartYtelseSaksopplysning,
                vurdering,
            )

        private fun vilkårsvurder(ytelseSaksopplysning: LivsoppholdYtelseSaksopplysning): LivsoppholdDelVurdering {
            if (ytelseSaksopplysning.vilkår in listOf(LivsoppholdDelVilkår.AAP, LivsoppholdDelVilkår.DAGPENGER) &&
                ytelseSaksopplysning.kilde != Kilde.SAKSB
            ) {
                return LivsoppholdDelVurdering(
                    ytelseSaksopplysning.vilkår,
                    Periodisering(
                        Utfall.UAVKLART,
                        ytelseSaksopplysning.harYtelse.totalePeriode,
                    ),
                    ytelseSaksopplysning.detaljer,
                )
            }

            return LivsoppholdDelVurdering(
                delVilkår = ytelseSaksopplysning.vilkår,
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
