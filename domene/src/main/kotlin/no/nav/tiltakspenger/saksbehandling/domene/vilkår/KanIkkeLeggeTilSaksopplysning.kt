package no.nav.tiltakspenger.saksbehandling.domene.vilkår

sealed interface KanIkkeLeggeTilSaksopplysning {
    data object MåVæreSaksbehandler : KanIkkeLeggeTilSaksopplysning
    data object PeriodenMåVæreLikVurderingsperioden : KanIkkeLeggeTilSaksopplysning
}
