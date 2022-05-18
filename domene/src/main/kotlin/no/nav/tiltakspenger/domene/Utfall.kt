package no.nav.tiltakspenger.domene

sealed class Utfall {
    class IkkeVurdert(val periode: Periode) : Utfall()
    class VurdertOgOppfylt(val periode: Periode) : Utfall()
    class VurdertOgIkkeOppfylt(val periode: Periode) : Utfall()
    class VurdertOgTrengerManuellBehandling(val periode: Periode) : Utfall()
}
