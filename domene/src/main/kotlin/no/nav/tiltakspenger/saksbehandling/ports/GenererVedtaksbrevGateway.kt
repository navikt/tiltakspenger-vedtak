package no.nav.tiltakspenger.saksbehandling.ports

import arrow.core.Either
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface GenererVedtaksbrevGateway {
    suspend fun genererVedtaksbrev(vedtak: Rammevedtak): Either<KunneIkkeGenererePdf, PdfOgJson>
}

object KunneIkkeGenererePdf
