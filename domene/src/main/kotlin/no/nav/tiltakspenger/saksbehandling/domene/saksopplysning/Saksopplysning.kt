package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

data class Saksopplysning(
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val harYtelseSaksopplysning: Periodisering<HarYtelseSaksopplysning?>,
    val saksbehandler: String? = null,
) {
    companion object {

        fun lagSaksopplysningFraSBH(
            fom: LocalDate,
            tom: LocalDate,
            vilkår: Vilkår,
            detaljer: String,
            harYtelseSaksopplysning: HarYtelseSaksopplysning,
            saksbehandler: String,
        ): Saksopplysning {
            return Saksopplysning(
                vilkår = vilkår,
                kilde = Kilde.SAKSB,
                detaljer = detaljer, // TODO: Her blir detaljer brukt til begrunnelse, bør kanskje revurderes
                harYtelseSaksopplysning = Periodisering(harYtelseSaksopplysning, Periode(fom, tom)),
                saksbehandler = saksbehandler,
            )
        }
    }
}
