package no.nav.tiltakspenger.vedtak.service.ports

import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface MultiRepo {
    suspend fun <T> lagreOgKjør(
        iverksattBehandling: BehandlingIverksatt,
        attestering: Attestering,
        vedtak: Vedtak,
        operasjon: suspend () -> T,
    ): T

    fun lagre(behandling: BehandlingVilkårsvurdert, attestering: Attestering)
}