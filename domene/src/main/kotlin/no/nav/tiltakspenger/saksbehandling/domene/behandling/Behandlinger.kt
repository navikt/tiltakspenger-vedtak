package no.nav.tiltakspenger.saksbehandling.domene.behandling

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf

/**
 * En samling av alle behandlinger innenfor en gitt sak.
 * Garanterer at første elementet er en førstegangsbehandling og de resterende revurderinger.
 */
data class Behandlinger(
    val behandlinger: NonEmptyList<Behandling>,
) : List<Behandling> by behandlinger {

    constructor(behandling: Behandling) : this(nonEmptyListOf(behandling))

    val sakId = behandlinger.distinctBy { it.sakId }.single()
    val revurderinger: Revurderinger = Revurderinger(behandlinger.drop(1))
    val førstegangsbehandling: Behandling = behandlinger.first().also {
        require(it.erFørstegangsbehandling)
    }

    fun leggTilRevurdering(
        behandling: Behandling,
    ): Behandlinger {
        val behandlinger = this.behandlinger + behandling
        return this.copy(behandlinger = behandlinger)
    }
}
