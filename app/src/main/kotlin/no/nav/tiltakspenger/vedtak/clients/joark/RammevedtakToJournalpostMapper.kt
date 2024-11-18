package no.nav.tiltakspenger.vedtak.clients.joark

import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.libs.json.objectMapper
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.vedtak.clients.joark.JoarkRequest.JournalpostDokument.DokumentVariant.ArkivPDF
import no.nav.tiltakspenger.vedtak.clients.joark.JoarkRequest.JournalpostDokument.DokumentVariant.OriginalJson

internal fun Rammevedtak.toJournalpostRequest(
    pdfOgJson: PdfOgJson,
): String {
    val tittel = "Vedtak om tiltakspenger"
    return JoarkRequest(
        tittel = tittel,
        journalpostType = JoarkRequest.JournalPostType.UTGAAENDE,
        // Utsendingskanal. Forsendelsen er distribuert digitalt til brukers meldingsboks i nav.no.
        kanal = "NAV_NO",
        avsenderMottaker = JoarkRequest.AvsenderMottaker(this.fnr.verdi),
        bruker = JoarkRequest.Bruker(this.fnr.verdi),
        // TODO bruk saksnummer i stedet for sakId
        sak = JoarkRequest.Sak.Fagsak(this.sakId.toString()),
        dokumenter = listOf(
            JoarkRequest.JournalpostDokument(
                tittel = tittel,
                brevkode = "MELDEKORT-TILTAKSPENGER",
                dokumentvarianter = listOf(
                    ArkivPDF(
                        fysiskDokument = pdfOgJson.pdfAsBase64(),
                        tittel = tittel,
                    ),
                    OriginalJson(
                        fysiskDokument = pdfOgJson.jsonAsBase64(),
                        tittel = tittel,
                    ),
                ),
            ),
        ),
        eksternReferanseId = this.id.toString(),
    ).let { objectMapper.writeValueAsString(it) }
}
