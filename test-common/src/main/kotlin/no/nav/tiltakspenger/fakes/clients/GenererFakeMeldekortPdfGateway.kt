package no.nav.tiltakspenger.fakes.clients

import no.nav.tiltakspenger.felles.PdfA
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak

class GenererFakeMeldekortPdfGateway : GenererMeldekortPdfGateway {
    private val response by lazy { PdfOgJson(PdfA("pdf".toByteArray()), "json") }
    override suspend fun genererMeldekortPdf(
        utbetalingsvedtak: Utbetalingsvedtak,
        hentSaksbehandlersNavn: suspend (String) -> String,
    ): PdfOgJson {
        return response
    }
}
