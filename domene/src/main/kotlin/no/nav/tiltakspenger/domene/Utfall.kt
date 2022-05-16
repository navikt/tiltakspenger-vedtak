package no.nav.tiltakspenger.domene

sealed class Utfall {
    class IkkeVurdert : Utfall()
    class VurdertOgOppfylt(val vilkårOppfyltPeriode: List<Periode>) : Utfall()
    class VurdertOgIkkeOppfylt : Utfall()
    class VurdertOgTrengerManuellBehandling : Utfall()
}
