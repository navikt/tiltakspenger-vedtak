package no.nav.tiltakspenger.fakes.clients

import no.nav.tiltakspenger.felles.PdfA
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway

class GenererFakeMeldekortPdfGateway : GenererMeldekortPdfGateway {
    private val response by lazy { PdfOgJson(PdfA("pdf".toByteArray()), "json") }
    override fun genererMeldekortPdf(meldekort: Meldekort.UtfyltMeldekort): PdfOgJson {
        return response
    }
}
