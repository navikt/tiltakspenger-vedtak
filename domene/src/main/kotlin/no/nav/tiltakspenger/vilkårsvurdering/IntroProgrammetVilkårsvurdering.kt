package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

data class Vurdering(
    val kilde: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
)

class IntroProgrammetVilkårsvurdering(
    søknad: Søknad
) {
    val søknadVurdering = Vurdering(
        kilde = "Søknad",
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = if (!søknad.deltarIntroduksjonsprogrammet) Utfall.OPPFYLT else Utfall.IKKE_OPPFYLT
    )
    var manuellVurdering: Vurdering? = null

    fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall) {
        manuellVurdering = Vurdering(kilde = "Saksbehandler", fom = fom, tom = tom, utfall = utfall)
    }

    fun samletUtfall(): Utfall = manuellVurdering?.utfall ?: søknadVurdering.utfall
}
