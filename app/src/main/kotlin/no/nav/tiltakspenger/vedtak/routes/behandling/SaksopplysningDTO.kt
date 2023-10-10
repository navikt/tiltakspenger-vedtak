package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

data class SaksopplysningDTO(
    val fom: String,
    val tom: String,
    val vilkår: String,
    val begrunnelse: String,
    val harYtelse: Boolean,
) {
    companion object {
        fun lagSaksopplysningMedVilkår(saksopplysning: SaksopplysningDTO): Saksopplysning {
            val vilkår = when (saksopplysning.vilkår) {
                "AAP" -> Vilkår.AAP
                "DAGPENGER" -> Vilkår.DAGPENGER
                else -> throw IllegalStateException("Kan ikke lage saksopplysning for vilkår ${saksopplysning.vilkår}")
            }

            return Saksopplysning.lagSaksopplysningFraSBH(
                fom = LocalDate.parse(saksopplysning.fom),
                tom = LocalDate.parse(saksopplysning.tom),
                vilkår = vilkår,
                detaljer = saksopplysning.begrunnelse,
                typeSaksopplysning = if (saksopplysning.harYtelse) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
            )
        }
    }
}
