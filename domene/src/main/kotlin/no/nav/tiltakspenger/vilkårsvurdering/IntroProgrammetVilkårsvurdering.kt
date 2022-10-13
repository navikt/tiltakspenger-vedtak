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
    val vurderinger: List<Vurdering> = listOf(
        Vurdering(
            kilde = "Søknad",
            fom = søknad.introduksjonsprogrammetDetaljer?.fom,
            tom = søknad.introduksjonsprogrammetDetaljer?.tom,
            utfall = if (!søknad.deltarIntroduksjonsprogrammet) Utfall.OPPFYLT else Utfall.IKKE_OPPFYLT
        )
    )

}
