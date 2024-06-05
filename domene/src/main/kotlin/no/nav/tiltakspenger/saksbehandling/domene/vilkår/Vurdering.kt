package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate

data class Vurdering(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
    val detaljer: String,
    val grunnlagId: String?,
)
