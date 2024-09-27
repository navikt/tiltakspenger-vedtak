package no.nav.tiltakspenger.fakes.clients

import arrow.core.Either
import arrow.core.right
import no.nav.tiltakspenger.felles.PdfA
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.KunneIkkeGenererePdf

class GenererFakeVedtaksbrevGateway : GenererVedtaksbrevGateway {
    private val response by lazy { PdfOgJson(PdfA("pdf".toByteArray()), "json").right() }
    override suspend fun genererVedtaksbrev(vedtak: Rammevedtak): Either<KunneIkkeGenererePdf, PdfOgJson> {
        return response
    }
}
