package no.nav.tiltakspenger.domene.behandling

sealed interface Revurderingsbehandling : Behandling {
    val forrigeBehandling: Førstegangsbehandling // todo HER MÅ VI STØTTE FLERE CASER todo //

}
