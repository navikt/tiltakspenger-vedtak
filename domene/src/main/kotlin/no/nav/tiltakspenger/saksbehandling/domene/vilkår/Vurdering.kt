package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate

/**
 * TODO jah: Forsvinner når vi har flyttet alt til Vilkår. Det kan godt hende Vurderingskonseptet beholdes, men ikke i den formen her.
 */
data class Vurdering(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
    val detaljer: String,
    val grunnlagId: String?,
)

/**
 * TODO jah: Denne slettes sammen med Vurdering, men vi gjør et forsøk på se at ytterpunktene er like de andre vilkårene.
 */
fun List<Vurdering>.totalePeriode(): Periode? {
    return this.mapNotNull { it.fom }.minOrNull()?.let { fom ->
        this.mapNotNull { it.tom }.maxOrNull()?.let { tom ->
            Periode(fom, tom)
        }
    }
}
