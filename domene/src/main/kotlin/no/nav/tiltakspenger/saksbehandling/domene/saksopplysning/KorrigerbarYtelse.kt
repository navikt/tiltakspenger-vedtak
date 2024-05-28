package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class KorrigerbarYtelse private constructor(
    val vurderingsperiode: Periode,
    val vilkår: Vilkår,
    val opprinneligSaksopplysning: Saksopplysning,
    val korrigertSaksopplysning: Saksopplysning?,
    val avklartSaksopplysning: Saksopplysning,
    val vurdering: Vurdering,
) {
    fun lagSaksopplysningFraPeriodeSpørsmål(
        vilkår: Vilkår,
        periodeSpm: Søknad.PeriodeSpm,
        periode: Periode,
    ): KorrigerbarYtelse {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligSaksopplysning = opprinneligSaksopplysning.copy(
                harYtelseSaksopplysning = if (periodeSpm is Søknad.PeriodeSpm.Ja) {
                    opprinneligSaksopplysning.harYtelseSaksopplysning
                        .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_IKKE_YTELSE, periode)
                        .setVerdiForDelPeriode(
                            HarYtelseSaksopplysning.HAR_YTELSE,
                            periodeSpm.periode,
                        )
                } else {
                    opprinneligSaksopplysning.harYtelseSaksopplysning
                        .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_IKKE_YTELSE, periode)
                },
            ),
        ).faktaavklar().vilkårsvurder()
    }

    private fun faktaavklar(): KorrigerbarYtelse {
        return this.copy(
            avklartSaksopplysning = korrigertSaksopplysning ?: opprinneligSaksopplysning,
        )
    }

    private fun vilkårsvurder(): KorrigerbarYtelse {
        return this.copy(vurdering = vilkårsvurder(this.avklartSaksopplysning))
    }

    fun periodiseringAvSaksopplysningOgUtfall(): Periodisering<SaksopplysningOgUtfallForPeriode> {
        return avklartSaksopplysning.harYtelseSaksopplysning.kombiner(vurdering.utfall) { harYtelse, utfall ->
            SaksopplysningOgUtfallForPeriode(
                avklartSaksopplysning.vilkår,
                avklartSaksopplysning.kilde,
                avklartSaksopplysning.detaljer,
                avklartSaksopplysning.saksbehandler,
                harYtelse ?: HarYtelseSaksopplysning.IKKE_INNHENTET_ENDA,
                utfall,
            )
        }
    }

    fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: Vilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        periode: Periode,
    ): KorrigerbarYtelse {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligSaksopplysning = opprinneligSaksopplysning.copy(
                harYtelseSaksopplysning = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
                    opprinneligSaksopplysning.harYtelseSaksopplysning
                        .setVerdiForDelPeriode(
                            HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
                            periode,
                        )
                        .setVerdiForDelPeriode(
                            HarYtelseSaksopplysning.HAR_YTELSE,
                            Periode(fraOgMedDatoSpm.fra, periode.til),
                        )
                } else {
                    opprinneligSaksopplysning.harYtelseSaksopplysning.setVerdiForDelPeriode(
                        HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
                        periode,
                    )
                },
            ),
        ).faktaavklar().vilkårsvurder()
    }

    fun lagSaksopplysningFraJaNeiSpørsmål(
        vilkår: Vilkår,
        jaNeiSpm: Søknad.JaNeiSpm,
        periode: Periode,
    ): KorrigerbarYtelse {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligSaksopplysning = opprinneligSaksopplysning.copy(
                harYtelseSaksopplysning = opprinneligSaksopplysning.harYtelseSaksopplysning.setVerdiForDelPeriode(
                    if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelseSaksopplysning.HAR_YTELSE else HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
                    periode,
                ),
            ),
        ).faktaavklar().vilkårsvurder()
    }

    fun oppdaterSaksopplysning(saksopplysning: Saksopplysning): KorrigerbarYtelse {
        return this.copy(
            korrigertSaksopplysning = saksopplysning,
        )
    }

    companion object {
        operator fun invoke(vurderingsperiode: Periode, vilkår: Vilkår): KorrigerbarYtelse {
            val tomSaksopplysning = Saksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelseSaksopplysning = Periodisering(null, vurderingsperiode),
            )
            return KorrigerbarYtelse(
                vurderingsperiode,
                vilkår,
                tomSaksopplysning,
                tomSaksopplysning,
                tomSaksopplysning,
                vilkårsvurder(tomSaksopplysning),
            )
        }

        private fun vilkårsvurder(saksopplysning: Saksopplysning): Vurdering {
            if (saksopplysning.vilkår in listOf(Vilkår.AAP, Vilkår.DAGPENGER) &&
                saksopplysning.kilde != Kilde.SAKSB
            ) {
                return Vurdering(
                    saksopplysning.vilkår,
                    saksopplysning.kilde,
                    Periodisering(
                        Utfall.KREVER_MANUELL_VURDERING,
                        saksopplysning.harYtelseSaksopplysning.totalePeriode,
                    ),
                    saksopplysning.detaljer,
                    null,
                )
            }

            return Vurdering(
                vilkår = saksopplysning.vilkår,
                kilde = saksopplysning.kilde,
                detaljer = saksopplysning.detaljer,
                grunnlagId = null,
                utfall = saksopplysning.harYtelseSaksopplysning.map {
                    when (it) {
                        HarYtelseSaksopplysning.HAR_YTELSE -> Utfall.IKKE_OPPFYLT
                        HarYtelseSaksopplysning.HAR_IKKE_YTELSE -> Utfall.OPPFYLT
                        else -> Utfall.KREVER_MANUELL_VURDERING
                    }
                },
            )
        }
    }
}

data class SaksopplysningOgUtfallForPeriode(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String?,
    val harYtelse: HarYtelseSaksopplysning,
    val utfall: Utfall,
)
