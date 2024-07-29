package no.nav.tiltakspenger.saksbehandling.domene.behandling

data class BehandlingerForBenk(
    val behandlinger: List<Førstegangsbehandling>,
    val søknader: List<Søknad>,
)
