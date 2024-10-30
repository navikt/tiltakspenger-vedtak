package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.meldekort.domene.Meldekort

interface GenererMeldekortPdfGateway {
    suspend fun genererMeldekortPdf(
        meldekort: Meldekort.UtfyltMeldekort,
        hentSaksbehandlersNavn: suspend (String) -> String,
    ): PdfOgJson
}
