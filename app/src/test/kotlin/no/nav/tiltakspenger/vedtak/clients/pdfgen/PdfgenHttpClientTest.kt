package no.nav.tiltakspenger.vedtak.clients.pdfgen

import no.nav.tiltakspenger.objectmothers.ObjectMother
import org.junit.jupiter.api.Test

class PdfgenHttpClientTest {

    @Test
    fun genererMeldekortPdf() {
        val meldekort = ObjectMother.utfyltMeldekort()
        val pdf = PdfgenHttpClient("unused").genererMeldekortPdf(meldekort)
    }
}
