package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.ytelser

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.Inngangsvilkårsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.SaksopplysningOgUtfallForPeriode

data class YtelseVilkårData private constructor(
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

    fun oppdaterSaksopplysning(ytelseSaksopplysning: YtelseSaksopplysning): YtelseVilkårData {
        return this.copy(
            korrigertYtelseSaksopplysning = ytelseSaksopplysning,
        ).faktaavklar().vilkårsvurder()
    }

    private fun faktaavklar(): YtelseVilkårData {
        return this.copy(
            avklartYtelseSaksopplysning = korrigertYtelseSaksopplysning
                ?: opprinneligYtelseSaksopplysning,
        )
    }

    private fun vilkårsvurder(): YtelseVilkårData {
        return this.copy(vurdering = vilkårsvurder(this.avklartYtelseSaksopplysning))
    }

    // TODO: Denne er ment å være midlertidig. Kanskje..?
    fun periodiseringAvSaksopplysningOgUtfall(): Periodisering<SaksopplysningOgUtfallForPeriode> {
        return avklartYtelseSaksopplysning.harYtelse.kombiner(vurdering.utfall) { harYtelse, utfall ->
            SaksopplysningOgUtfallForPeriode(
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

        operator fun invoke(vurderingsperiode: Periode, vilkår: Inngangsvilkår): YtelseVilkårData {
            val tomYtelseSaksopplysning = YtelseSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
            )
            return YtelseVilkårData(
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
        ): YtelseVilkårData =
            YtelseVilkårData(
                vurderingsperiode,
                vilkår,
                opprinneligYtelseSaksopplysning,
                korrigertYtelseSaksopplysning,
                avklartYtelseSaksopplysning,
                vurdering,
            )

        private fun vilkårsvurder(ytelseSaksopplysning: YtelseSaksopplysning): Vurdering {
            return Vurdering(
                vilkår = ytelseSaksopplysning.vilkår,
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
