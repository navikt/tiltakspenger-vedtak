package no.nav.tiltakspenger.vedtak.repository.attestering

import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.felles.BehandlingId

interface AttesteringRepo {
    fun lagre(attestering: Attestering): Attestering
    fun hentForBehandling(behandlingId: BehandlingId): List<Attestering>
}
