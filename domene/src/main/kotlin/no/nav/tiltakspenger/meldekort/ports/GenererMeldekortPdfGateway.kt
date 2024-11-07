package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak

interface GenererMeldekortPdfGateway {
    suspend fun genererMeldekortPdf(
        utbetalingsvedtak: Utbetalingsvedtak,
        hentSaksbehandlersNavn: suspend (String) -> String,
    ): PdfOgJson
}
