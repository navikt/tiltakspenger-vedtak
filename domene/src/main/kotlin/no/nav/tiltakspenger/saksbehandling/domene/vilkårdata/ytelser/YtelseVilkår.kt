package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.ytelser

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.Inngangsvilkårsbehandling

data class YtelseVilkår private constructor(
    val vurderingsperiode: Periode,
    val vilkår: Inngangsvilkår,
    val opprinneligYtelseSaksopplysning: YtelseSaksopplysning,
    val korrigertYtelseSaksopplysning: YtelseSaksopplysning?,
    val avklartYtelseSaksopplysning: YtelseSaksopplysning,
    val vurdering: Vurdering,
) : Inngangsvilkårsbehandling {

    override fun vilkår(): Inngangsvilkår {
        return vilkår
    }

    override fun vurdering(): Vurdering = vurdering

    fun oppdaterSaksopplysning(ytelseSaksopplysning: YtelseSaksopplysning): YtelseVilkår {
        return this.copy(
            korrigertYtelseSaksopplysning = ytelseSaksopplysning,
        ).faktaavklar().vilkårsvurder()
    }

    private fun faktaavklar(): YtelseVilkår {
        return this.copy(
            avklartYtelseSaksopplysning = korrigertYtelseSaksopplysning
                ?: opprinneligYtelseSaksopplysning,
        )
    }

    private fun vilkårsvurder(): YtelseVilkår {
        return this.copy(vurdering = vilkårsvurder(this.avklartYtelseSaksopplysning))
    }

    companion object {

        operator fun invoke(vurderingsperiode: Periode, vilkår: Inngangsvilkår): YtelseVilkår {
            val tomYtelseSaksopplysning = YtelseSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
            )
            return YtelseVilkår(
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
            vilkår: Inngangsvilkår,
            opprinneligYtelseSaksopplysning: YtelseSaksopplysning,
            korrigertYtelseSaksopplysning: YtelseSaksopplysning?,
            avklartYtelseSaksopplysning: YtelseSaksopplysning,
            vurdering: Vurdering,
        ): YtelseVilkår =
            YtelseVilkår(
                vurderingsperiode,
                vilkår,
                opprinneligYtelseSaksopplysning,
                korrigertYtelseSaksopplysning,
                avklartYtelseSaksopplysning,
                vurdering,
            )

        private fun vilkårsvurder(ytelseSaksopplysning: YtelseSaksopplysning): Vurdering {
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
