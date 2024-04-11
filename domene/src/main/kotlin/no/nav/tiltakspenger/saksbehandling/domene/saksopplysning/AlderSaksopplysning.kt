package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

data class AlderSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val fødselsdato: LocalDate,
) : SaksopplysningInterface

fun List<AlderSaksopplysning>.vilkårsvurder(vurderingsperiode: Periode): List<Vurdering> {
    check(this.size == 1) { "Det må være akkurat én avklart saksopplysning for søkers alder" }
    val saksopplysning = this.first()
    val datoBrukerFyller18 = saksopplysning.fødselsdato.plusYears(18)
    val perioder = vurderingsperiode.splittFramTil(datoBrukerFyller18)
    return perioder.map {
        val brukerHarRettIPerioden = datoBrukerFyller18.isBefore(it.til)
        Vurdering(
            vilkår = saksopplysning.vilkår,
            kilde = saksopplysning.kilde,
            fom = it.fra,
            tom = it.til,
            utfall = if (brukerHarRettIPerioden) Utfall.OPPFYLT else Utfall.IKKE_OPPFYLT,
            detaljer = if (brukerHarRettIPerioden) "Bruker er under 18 år" else "Bruker er over 18 år",
        )
    }
}
