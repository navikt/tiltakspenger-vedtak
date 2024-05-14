package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class BarnSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val oppholderSegIEØS: Boolean,
    val fødselsdato: LocalDate,
    val ident: String?,
    val manueltRegistrert: Boolean,
) : SaksopplysningInterface

fun List<BarnSaksopplysning>.vilkårsvurder(vurderingsperiode: Periode): List<Vurdering> {
    return this.map { saksopplysning ->
        val datoBarnFyller16 = saksopplysning.fødselsdato.plusYears(16)
        val perioder = vurderingsperiode.splittFramTil(datoBarnFyller16)
        return perioder.map { periode ->
            val barnGirIkkeRett = datoBarnFyller16.isBefore(periode.til) || !saksopplysning.oppholderSegIEØS

            val utfall = if (saksopplysning.manueltRegistrert) {
                Utfall.KREVER_MANUELL_VURDERING
            } else if (barnGirIkkeRett) {
                Utfall.IKKE_OPPFYLT
            } else {
                Utfall.OPPFYLT
            }

            Vurdering(
                vilkår = saksopplysning.vilkår,
                kilde = saksopplysning.kilde,
                fom = periode.fra,
                tom = periode.til,
                utfall = utfall,
                detaljer = "TODO",
            )
        }
    }.flatten()
}
