package no.nav.tiltakspenger.fakes.clients

import no.nav.tiltakspenger.felles.PdfA
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn

class GenererFakeMeldekortPdfGateway : GenererMeldekortPdfGateway {
    private val response by lazy { PdfOgJson(PdfA("pdf".toByteArray()), "json") }
    override suspend fun genererMeldekortPdf(
        meldekort: Meldekort.UtfyltMeldekort,
        hentNavn: suspend (Fnr) -> Navn,
    ): PdfOgJson {
        return response
    }
}
