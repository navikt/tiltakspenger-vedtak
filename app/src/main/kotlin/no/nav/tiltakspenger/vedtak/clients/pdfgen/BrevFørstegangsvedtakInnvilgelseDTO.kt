package no.nav.tiltakspenger.vedtak.clients.pdfgen

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.vedtak.clients.pdfgen.formattering.norskDatoFormatter
import no.nav.tiltakspenger.vedtak.db.serialize
import java.time.LocalDate

@Suppress("unused")
private class BrevFørstegangsvedtakInnvilgelseDTO(
    val personalia: BrevPersonaliaDTO,
    val tiltaksnavn: String,
    val rammevedtakFraDato: String,
    val rammevedtakTilDato: String,
    val saksnummer: String,
    val barnetillegg: Boolean,
    val saksbehandlerNavn: String,
    val beslutterNavn: String,
    val kontor: String,
    val datoForUtsending: String,
    val sats: Int,
    val satsBarn: Int,
)

internal suspend fun Rammevedtak.tobrevDTO(
    hentNavn: suspend (Fnr) -> Navn,
): String {
    val navn = hentNavn(fnr)
    return BrevFørstegangsvedtakInnvilgelseDTO(
        personalia = BrevPersonaliaDTO(
            ident = this.fnr.verdi,
            fornavn = navn.fornavn,
            etternavn = navn.mellomnavnOgEtternavn,
            antallBarn = 0,
        ),
        tiltaksnavn = "TODO pre-mvp",
        rammevedtakFraDato = periode.fraOgMed.format(norskDatoFormatter),
        rammevedtakTilDato = periode.tilOgMed.format(norskDatoFormatter),
        saksnummer = saksnummer.verdi,
        barnetillegg = false,
        saksbehandlerNavn = "TODO pre-mvp beslutterNavn",
        beslutterNavn = "TODO pre-mvp beslutterNavn",
        kontor = "TODO pre-mvp: Dette bør ligge på behandlingen. Se NORG-oppgave i trello.",
        // Dette er vår dato, det brukes typisk når bruker klager på vedtaksbrev på dato ...
        datoForUtsending = LocalDate.now().format(norskDatoFormatter),
        // TODO pre-mvp jah: Dette må ligge i behandlingen. Men dette vil kunne være en periodisering. Si f.eks. vedtaket går fra slutten av desember.
        sats = 285,
        satsBarn = 53,
    ).let { serialize(it) }
}
