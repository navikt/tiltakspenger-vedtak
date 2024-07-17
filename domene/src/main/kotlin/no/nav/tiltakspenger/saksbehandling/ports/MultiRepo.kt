package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.service.statistikk.SakStatistikkDTO

interface MultiRepo {
    suspend fun lagreOgKjør(
        iverksattBehandling: Behandling,
        attestering: Attestering,
        vedtak: Vedtak,
        statistikk: SakStatistikkDTO,
        operasjon: suspend () -> String,
    ): String

    fun lagre(behandling: Behandling, attestering: Attestering)
}
