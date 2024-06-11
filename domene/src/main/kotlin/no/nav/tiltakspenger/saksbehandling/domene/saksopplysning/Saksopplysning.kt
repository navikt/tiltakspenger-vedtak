package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning.HAR_IKKE_YTELSE
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning.HAR_YTELSE
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning.IKKE_INNHENTET_ENDA
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class Saksopplysning(
    val fom: LocalDate,
    val tom: LocalDate,
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val typeSaksopplysning: TypeSaksopplysning,
    val saksbehandler: String? = null,
) {
    companion object {
        fun saksopplysningIkkeInnhentet(periode: Periode, vilkår: Vilkår): Saksopplysning {
            return Saksopplysning(
                fom = periode.fra,
                tom = periode.til,
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                typeSaksopplysning = IKKE_INNHENTET_ENDA,
            )
        }

        fun lagSaksopplysningFraSBH(
            fom: LocalDate,
            tom: LocalDate,
            vilkår: Vilkår,
            detaljer: String,
            typeSaksopplysning: TypeSaksopplysning,
            saksbehandler: String,
        ): Saksopplysning {
            return Saksopplysning(
                fom = fom,
                tom = tom,
                vilkår = vilkår,
                kilde = Kilde.SAKSB,
                detaljer = detaljer, // TODO: Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
                typeSaksopplysning = typeSaksopplysning,
                saksbehandler = saksbehandler,
            )
        }
    }

    fun lagVurdering(periode: Periode): Vurdering {
        val manuell = Periodisering(Utfall.KREVER_MANUELL_VURDERING, periode)
        val ikkeOppfylt = Periodisering(Utfall.IKKE_OPPFYLT, periode)

        val vurdering = when (this.typeSaksopplysning) {
            IKKE_INNHENTET_ENDA -> Vurdering(
                vilkår = this.vilkår,
                detaljer = this.detaljer,
                utfall = manuell,
            )

            // TODO: Hvorfor setter man ikke OPPFYLT for hele vurderingsperioden?
            HAR_IKKE_YTELSE -> Vurdering(
                vilkår = this.vilkår,
                detaljer = this.detaljer,
                utfall = manuell.setVerdiForDelPeriode(Utfall.OPPFYLT, Periode(this.fom, this.tom)),
            )

            HAR_YTELSE -> {
                if (this.vilkår in listOf(Vilkår.AAP, Vilkår.DAGPENGER) && this.kilde == Kilde.SAKSB) {
                    Vurdering(
                        vilkår = this.vilkår,
                        detaljer = this.detaljer,
                        utfall = manuell,
                    )
                } else {
                    Vurdering(
                        vilkår = this.vilkår,
                        detaljer = this.detaljer,
                        utfall = ikkeOppfylt.setVerdiForDelPeriode(Utfall.IKKE_OPPFYLT, Periode(this.fom, this.tom)),
                    )
                }
            }
        }
        return vurdering
    }
}
