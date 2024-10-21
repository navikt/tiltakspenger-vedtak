package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn

interface GenererMeldekortPdfGateway {
    suspend fun genererMeldekortPdf(
        meldekort: Meldekort.UtfyltMeldekort,
        hentBrukersNavn: suspend (Fnr) -> Navn,
    ): PdfOgJson
}
