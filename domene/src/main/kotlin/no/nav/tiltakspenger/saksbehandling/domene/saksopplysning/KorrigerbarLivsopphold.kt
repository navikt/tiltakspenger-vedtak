package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class KorrigerbarLivsopphold private constructor(
    val vurderingsperiode: Periode,
    val vilkår: Vilkår,
    val opprinneligLivoppholdSaksopplysning: LivoppholdSaksopplysning,
    val korrigertLivoppholdSaksopplysning: LivoppholdSaksopplysning?,
    val avklartLivoppholdSaksopplysning: LivoppholdSaksopplysning,
    val vurdering: Vurdering,
) {
    fun lagSaksopplysningFraPeriodeSpørsmål(
        vilkår: Vilkår,
        periodeSpm: Søknad.PeriodeSpm,
        periode: Periode,
    ): KorrigerbarLivsopphold {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligLivoppholdSaksopplysning = opprinneligLivoppholdSaksopplysning.copy(
                harYtelse = if (periodeSpm is Søknad.PeriodeSpm.Ja) {
                    opprinneligLivoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, periode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            periodeSpm.periode,
                        )
                } else {
                    opprinneligLivoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, periode)
                },
            ),
        ).faktaavklar().vilkårsvurder()
    }

    private fun faktaavklar(): KorrigerbarLivsopphold {
        return this.copy(
            avklartLivoppholdSaksopplysning = korrigertLivoppholdSaksopplysning ?: opprinneligLivoppholdSaksopplysning,
        )
    }

    private fun vilkårsvurder(): KorrigerbarLivsopphold {
        return this.copy(vurdering = vilkårsvurder(this.avklartLivoppholdSaksopplysning))
    }

    fun periodiseringAvSaksopplysningOgUtfall(): Periodisering<SaksopplysningOgUtfallForPeriode> {
        return avklartLivoppholdSaksopplysning.harYtelse.kombiner(vurdering.utfall) { harYtelse, utfall ->
            SaksopplysningOgUtfallForPeriode(
                avklartLivoppholdSaksopplysning.vilkår,
                avklartLivoppholdSaksopplysning.kilde,
                avklartLivoppholdSaksopplysning.detaljer,
                avklartLivoppholdSaksopplysning.saksbehandler,
                harYtelse,
                utfall,
            )
        }
    }

    fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: Vilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        periode: Periode,
    ): KorrigerbarLivsopphold {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligLivoppholdSaksopplysning = opprinneligLivoppholdSaksopplysning.copy(
                harYtelse = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
                    opprinneligLivoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_IKKE_YTELSE,
                            periode,
                        )
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            Periode(fraOgMedDatoSpm.fra, periode.til),
                        )
                } else {
                    opprinneligLivoppholdSaksopplysning.harYtelse.setVerdiForDelPeriode(
                        HarYtelse.HAR_IKKE_YTELSE,
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
    ): KorrigerbarLivsopphold {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligLivoppholdSaksopplysning = opprinneligLivoppholdSaksopplysning.copy(
                harYtelse = opprinneligLivoppholdSaksopplysning.harYtelse.setVerdiForDelPeriode(
                    if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                    periode,
                ),
            ),
        ).faktaavklar().vilkårsvurder()
    }

    fun oppdaterSaksopplysning(livoppholdSaksopplysning: LivoppholdSaksopplysning): KorrigerbarLivsopphold {
        return this.copy(
            korrigertLivoppholdSaksopplysning = livoppholdSaksopplysning,
        )
    }

    companion object {
        operator fun invoke(vurderingsperiode: Periode, vilkår: Vilkår): KorrigerbarLivsopphold {
            val tomLivoppholdSaksopplysning = LivoppholdSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(null, vurderingsperiode),
            )
            return KorrigerbarLivsopphold(
                vurderingsperiode,
                vilkår,
                tomLivoppholdSaksopplysning,
                tomLivoppholdSaksopplysning,
                tomLivoppholdSaksopplysning,
                vilkårsvurder(tomLivoppholdSaksopplysning),
            )
        }

        private fun vilkårsvurder(livoppholdSaksopplysning: LivoppholdSaksopplysning): Vurdering {
            if (livoppholdSaksopplysning.vilkår in listOf(Vilkår.AAP, Vilkår.DAGPENGER) &&
                livoppholdSaksopplysning.kilde != Kilde.SAKSB
            ) {
                return Vurdering(
                    livoppholdSaksopplysning.vilkår,
                    livoppholdSaksopplysning.kilde,
                    Periodisering(
                        Utfall.KREVER_MANUELL_VURDERING,
                        livoppholdSaksopplysning.harYtelse.totalePeriode,
                    ),
                    livoppholdSaksopplysning.detaljer,
                    null,
                )
            }

            return Vurdering(
                vilkår = livoppholdSaksopplysning.vilkår,
                kilde = livoppholdSaksopplysning.kilde,
                detaljer = livoppholdSaksopplysning.detaljer,
                grunnlagId = null,
                utfall = livoppholdSaksopplysning.harYtelse.map {
                    when (it) {
                        HarYtelse.HAR_YTELSE -> Utfall.IKKE_OPPFYLT
                        HarYtelse.HAR_IKKE_YTELSE -> Utfall.OPPFYLT
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
    val harYtelse: HarYtelse?,
    val utfall: Utfall,
)
