package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import java.time.LocalDate

data class KorrigerbareBarnDetaljer private constructor(
    val vurderingsperiode: Periode,
    val opprinneligSaksopplysning: BarnetilleggBarnDetaljer,
    val korrigertSaksopplysning: BarnetilleggBarnDetaljer?,
    val avklartSaksopplysning: BarnetilleggBarnDetaljer,
    val vurdering: Periodisering<Utfall>,
) : JaNeiPeriodeVurdering {

    override fun vurdering(): Periodisering<Utfall> = vurdering

    companion object {
        operator fun invoke(
            vurderingsperiode: Periode,
            saksopplysning: BarnetilleggBarnDetaljer,
        ): KorrigerbareBarnDetaljer {
            return KorrigerbareBarnDetaljer(
                vurderingsperiode = vurderingsperiode,
                opprinneligSaksopplysning = saksopplysning,
                korrigertSaksopplysning = null,
                avklartSaksopplysning = saksopplysning,
                vurdering = vilkårsvurder(vurderingsperiode, saksopplysning),
            )
        }

        private fun vilkårsvurder(
            vurderingsperiode: Periode,
            avklartSaksopplysning: BarnetilleggBarnDetaljer,
        ): Periodisering<Utfall> {
            val datoBlir16 = avklartSaksopplysning.fødselsdato.plusYears(16)
            val periodeEr16EllerMer = Periode(datoBlir16, LocalDate.MAX)
            val periodeErUnder16 = Periode(LocalDate.MIN, datoBlir16.minusDays(1))

            val periodisering = Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode)
            return when {
                vurderingsperiode.etter(datoBlir16) ->
                    periodisering.setVerdiForDelPeriode(Utfall.OPPFYLT, vurderingsperiode)

                vurderingsperiode.før(datoBlir16) ->
                    periodisering.setVerdiForDelPeriode(Utfall.IKKE_OPPFYLT, vurderingsperiode)

                else ->
                    periodisering
                        .setVerdiForDelPeriode(
                            Utfall.OPPFYLT,
                            vurderingsperiode.overlappendePeriode(periodeEr16EllerMer)!!,
                        )
                        .setVerdiForDelPeriode(
                            Utfall.IKKE_OPPFYLT,
                            vurderingsperiode.overlappendePeriode(periodeErUnder16)!!,
                        )
            }
        }
    }

    fun avklarFakta(): KorrigerbareBarnDetaljer = this.copy(
        avklartSaksopplysning = korrigertSaksopplysning ?: opprinneligSaksopplysning,
    )

    fun vilkårsvurder(): KorrigerbareBarnDetaljer =
        this.copy(vurdering = vilkårsvurder(vurderingsperiode, avklartSaksopplysning))
}
