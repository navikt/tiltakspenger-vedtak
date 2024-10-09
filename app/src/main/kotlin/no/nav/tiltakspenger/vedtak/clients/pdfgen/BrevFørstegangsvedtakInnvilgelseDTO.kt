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
    val tiltaksinfo: BrevTiltaksinfoDTO,
    val fraDato: String,
    val tilDato: String,
    val saksnummer: String,
    val barnetillegg: Boolean,
    val saksbehandler: String,
    val beslutter: String,
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
        tiltaksinfo = BrevTiltaksinfoDTO(
            tiltak = "TODO pre-mvp jah: Disse bør være lagret på behandlingen. Enten på vilkåret eller rett på behandlingen.",
            tiltaksnavn = "TODO pre-mvp",
            tiltaksnummer = "TODO pre-mvp",
            arrangør = "TODO pre-mvp",
        ),
        fraDato = periode.fraOgMed.format(norskDatoFormatter),
        tilDato = periode.tilOgMed.format(norskDatoFormatter),
        saksnummer = saksnummer.verdi,
        barnetillegg = false,
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        kontor = "TODO pre-mvp: Dette bør ligge på behandlingen. Se NORG-oppgave i trello.",
        // Dette er vår dato, det brukes typisk når bruker klager på vedtaksbrev på dato ...
        datoForUtsending = LocalDate.now().format(norskDatoFormatter),
        // TODO pre-mvp jah: Dette må ligge i behandlingen. Men dette vil kunne være en periodisering. Si f.eks. vedtaket går fra slutten av desember.
        sats = 285,
        satsBarn = 53,
    ).let { serialize(it) }
}
