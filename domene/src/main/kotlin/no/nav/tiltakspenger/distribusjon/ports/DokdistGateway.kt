package no.nav.tiltakspenger.distribusjon.ports

import arrow.core.Either
import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.CorrelationId

interface DokdistGateway {
    suspend fun distribuerDokument(
        journalpostId: JournalpostId,
        correlationId: CorrelationId,
    ): Either<KunneIkkeDistribuereDokument, DistribusjonId>
}

object KunneIkkeDistribuereDokument
