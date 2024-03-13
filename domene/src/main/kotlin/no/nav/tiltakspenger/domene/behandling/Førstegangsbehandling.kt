package no.nav.tiltakspenger.domene.behandling

sealed interface Førstegangsbehandling : Behandling {
    val søknader: List<Søknad>

    fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)
}
