package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface MultiRepo {
    suspend fun lagreOgKjør(
        iverksattBehandling: BehandlingIverksatt,
        attestering: Attestering,
        vedtak: Vedtak,
        operasjon: suspend () -> String,
    ): String

    fun lagre(behandling: BehandlingVilkårsvurdert, attestering: Attestering)
}
