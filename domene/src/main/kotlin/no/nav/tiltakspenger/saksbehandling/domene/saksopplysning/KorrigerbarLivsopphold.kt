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
    val opprinneligLivsoppholdSaksopplysning: LivsoppholdSaksopplysning,
    val korrigertLivsoppholdSaksopplysning: LivsoppholdSaksopplysning?,
    val avklartLivsoppholdSaksopplysning: LivsoppholdSaksopplysning,
    val vurdering: Vurdering,
) {
    fun lagSaksopplysningFraPeriodeSpørsmål(
        vilkår: Vilkår,
        periodeSpm: Søknad.PeriodeSpm,
        periode: Periode,
    ): KorrigerbarLivsopphold {
        require(vilkår == this.vilkår) { "Feil vilkår $vilkår for korrigerbare ytelse med vilkår ${this.vilkår}" }

        return this.copy(
            opprinneligLivsoppholdSaksopplysning = opprinneligLivsoppholdSaksopplysning.copy(
                harYtelse = if (periodeSpm is Søknad.PeriodeSpm.Ja) {
                    opprinneligLivsoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, periode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            periodeSpm.periode,
                        )
                } else {
                    opprinneligLivsoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, periode)
                },
            ),
        ).faktaavklar().vilkårsvurder()
    }

    private fun faktaavklar(): KorrigerbarLivsopphold {
        return this.copy(
            avklartLivsoppholdSaksopplysning = korrigertLivsoppholdSaksopplysning
                ?: opprinneligLivsoppholdSaksopplysning,
        )
    }

    private fun vilkårsvurder(): KorrigerbarLivsopphold {
        return this.copy(vurdering = vilkårsvurder(this.avklartLivsoppholdSaksopplysning))
    }

    // TODO: Denne er ment å være midlertidig. Kanskje..?
    fun periodiseringAvSaksopplysningOgUtfall(): Periodisering<SaksopplysningOgUtfallForPeriode> {
        return avklartLivsoppholdSaksopplysning.harYtelse.kombiner(vurdering.utfall) { harYtelse, utfall ->
            SaksopplysningOgUtfallForPeriode(
                avklartLivsoppholdSaksopplysning.vilkår,
                avklartLivsoppholdSaksopplysning.kilde,
                avklartLivsoppholdSaksopplysning.detaljer,
                avklartLivsoppholdSaksopplysning.saksbehandler,
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
            opprinneligLivsoppholdSaksopplysning = opprinneligLivsoppholdSaksopplysning.copy(
                harYtelse = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
                    opprinneligLivsoppholdSaksopplysning.harYtelse
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_IKKE_YTELSE,
                            periode,
                        )
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            Periode(fraOgMedDatoSpm.fra, periode.til),
                        )
                } else {
                    opprinneligLivsoppholdSaksopplysning.harYtelse.setVerdiForDelPeriode(
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
            opprinneligLivsoppholdSaksopplysning = opprinneligLivsoppholdSaksopplysning.copy(
                harYtelse = opprinneligLivsoppholdSaksopplysning.harYtelse.setVerdiForDelPeriode(
                    if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                    periode,
                ),
            ),
        ).faktaavklar().vilkårsvurder()
    }

    fun oppdaterSaksopplysning(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): KorrigerbarLivsopphold {
        return this.copy(
            korrigertLivsoppholdSaksopplysning = livsoppholdSaksopplysning,
        )
    }

    companion object {

        operator fun invoke(vurderingsperiode: Periode, vilkår: Vilkår): KorrigerbarLivsopphold {
            val tomLivsoppholdSaksopplysning = LivsoppholdSaksopplysning(
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                saksbehandler = null, // TODO: Bør være system?
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
            )
            return KorrigerbarLivsopphold(
                vurderingsperiode,
                vilkår,
                tomLivsoppholdSaksopplysning,
                tomLivsoppholdSaksopplysning,
                tomLivsoppholdSaksopplysning,
                vilkårsvurder(tomLivsoppholdSaksopplysning),
            )
        }

        fun fromDb(
            vurderingsperiode: Periode,
            vilkår: Vilkår,
            opprinneligLivsoppholdSaksopplysning: LivsoppholdSaksopplysning,
            korrigertLivsoppholdSaksopplysning: LivsoppholdSaksopplysning?,
            avklartLivsoppholdSaksopplysning: LivsoppholdSaksopplysning,
            vurdering: Vurdering,
        ): KorrigerbarLivsopphold =
            KorrigerbarLivsopphold(
                vurderingsperiode,
                vilkår,
                opprinneligLivsoppholdSaksopplysning,
                korrigertLivsoppholdSaksopplysning,
                avklartLivsoppholdSaksopplysning,
                vurdering,
            )

        private fun vilkårsvurder(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): Vurdering {
            if (livsoppholdSaksopplysning.vilkår in listOf(Vilkår.AAP, Vilkår.DAGPENGER) &&
                livsoppholdSaksopplysning.kilde != Kilde.SAKSB
            ) {
                return Vurdering(
                    livsoppholdSaksopplysning.vilkår,
                    Periodisering(
                        Utfall.KREVER_MANUELL_VURDERING,
                        livsoppholdSaksopplysning.harYtelse.totalePeriode,
                    ),
                    livsoppholdSaksopplysning.detaljer,
                )
            }

            return Vurdering(
                vilkår = livsoppholdSaksopplysning.vilkår,
                detaljer = livsoppholdSaksopplysning.detaljer,
                utfall = livsoppholdSaksopplysning.harYtelse.map {
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
    val harYtelse: HarYtelse,
    val utfall: Utfall,
)
