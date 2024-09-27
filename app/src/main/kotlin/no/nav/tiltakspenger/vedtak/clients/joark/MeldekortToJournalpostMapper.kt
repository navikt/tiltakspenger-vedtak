package no.nav.tiltakspenger.vedtak.clients.joark

import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.vedtak.clients.joark.JoarkRequest.JournalpostDokument.DokumentVariant.ArkivPDF
import no.nav.tiltakspenger.vedtak.clients.joark.JoarkRequest.JournalpostDokument.DokumentVariant.OriginalJson
import no.nav.tiltakspenger.vedtak.routes.objectMapper
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

internal fun Meldekort.toJournalpostRequest(
    pdfOgJson: PdfOgJson,
): String {
    val tittel = lagMeldekortTittel(this.periode)
    return JoarkRequest(
        tittel = tittel,
        journalpostType = JoarkRequest.JournalPostType.NOTAT,
        // I følge doccen, skal denne være null for NOTAT.
        kanal = null,
        avsenderMottaker = JoarkRequest.AvsenderMottaker(this.fnr.verdi),
        bruker = JoarkRequest.Bruker(this.fnr.verdi),
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

private fun lagMeldekortTittel(periode: Periode): String {
    // Meldekort for uke 5 - 6 (29.01.2024 - 11.02.2024)
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return "Meldekort for uke ${periode.fraOgMed.get(WeekFields.ISO.weekOfWeekBasedYear())}" +
        " - ${periode.tilOgMed.get(WeekFields.ISO.weekOfWeekBasedYear())}" +
        " (${periode.fraOgMed.format(formatter)} - ${periode.tilOgMed.format(formatter)})"
}