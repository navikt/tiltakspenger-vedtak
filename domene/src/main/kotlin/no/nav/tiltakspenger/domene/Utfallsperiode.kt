package no.nav.tiltakspenger.domene

data class Utfallsperiode(
    val utfall: Utfall,
    val periode: Periode
)

enum class Utfall {
    IkkeVurdert,
    VurdertOgOppfylt,
    VurdertOgIkkeOppfylt,
    VurdertOgTrengerManuellBehandling,
}
