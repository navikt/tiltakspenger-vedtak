package no.nav.tiltakspenger.distribusjon.domene

import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.VedtakId

data class VedtakSomSkalDistribueres(
    val id: VedtakId,
    val journalpostId: JournalpostId,
)
