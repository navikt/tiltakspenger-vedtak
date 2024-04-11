package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning.HAR_IKKE_YTELSE
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning.HAR_YTELSE
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.OppfyllbarVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class Inngangsvilkår(
    val vilårAAP: OppfyllbarVilkårData,
    val vilårDagpenger: OppfyllbarVilkårData,
    val vilårForeldrepenger: OppfyllbarVilkårData,
)

sealed interface SaksopplysningInterface {
    val kilde: Kilde
    val vilkår: Vilkår
    val detaljer: String
    val saksbehandler: String?
}

data class TiltakSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val fom: LocalDate,
    val tom: LocalDate,
    val tiltakId: String,
) : SaksopplysningInterface

data class Saksopplysning(
    val fom: LocalDate,
    val tom: LocalDate,
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val typeSaksopplysning: TypeSaksopplysning,
    val saksbehandler: String? = null,
    val tiltakId: String? = null,
) {
    companion object {
        fun saksopplysningIkkeInnhentet(periode: Periode, vilkår: Vilkår): Saksopplysning {
            return Saksopplysning(
                fom = periode.fra,
                tom = periode.til,
                vilkår = vilkår,
                kilde = vilkår.kilde(),
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
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

    fun lagVurdering(periode: Periode): List<Vurdering> {
        val vurdering = when (this.typeSaksopplysning) {
            TypeSaksopplysning.IKKE_INNHENTET_ENDA -> Vurdering(
                vilkår = this.vilkår,
                kilde = this.kilde,
                fom = this.fom,
                tom = this.tom,
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = this.detaljer,
            )

            HAR_YTELSE -> if (this.vilkår in listOf(Vilkår.AAP, Vilkår.DAGPENGER, Vilkår.TILTAKSPENGER)) {
                if (this.kilde == Kilde.SAKSB) {
                    Vurdering(
                        vilkår = this.vilkår,
                        kilde = this.kilde,
                        fom = this.fom,
                        tom = this.tom,
                        detaljer = this.detaljer,
                        utfall = Utfall.IKKE_OPPFYLT,
                    )
                } else {
                    Vurdering(
                        vilkår = this.vilkår,
                        kilde = this.kilde,
                        fom = this.fom,
                        tom = this.tom,
                        detaljer = this.detaljer,
                        utfall = Utfall.KREVER_MANUELL_VURDERING,
                    )
                }
            } else {
                Vurdering(
                    vilkår = this.vilkår,
                    kilde = this.kilde,
                    fom = this.fom,
                    tom = this.tom,
                    detaljer = this.detaljer,
                    utfall = Utfall.IKKE_OPPFYLT,
                )
            }

            HAR_IKKE_YTELSE -> Vurdering(
                vilkår = this.vilkår,
                kilde = this.kilde,
                fom = this.fom,
                tom = this.tom,
                detaljer = this.detaljer,
                utfall = Utfall.OPPFYLT,
            )
        }

        if (vurdering.utfall == Utfall.IKKE_OPPFYLT) {
            val oppfyltePerioder = periode.ikkeOverlappendePeriode(Periode(fra = this.fom, til = this.tom)).map {
                Vurdering(
                    vilkår = this.vilkår,
                    kilde = this.kilde,
                    fom = it.fra,
                    tom = it.til,
                    detaljer = this.detaljer,
                    utfall = Utfall.OPPFYLT,
                )
            }

            return oppfyltePerioder + vurdering
        }
        return listOf(vurdering)
    }
}
