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
    hentBrukersNavn: suspend (Fnr) -> Navn,
    hentSaksbehandlersNavn: suspend (String) -> String,
): String {
    val brukersNavn = hentBrukersNavn(fnr)
    val saksbehandlersNavn = hentSaksbehandlersNavn(saksbehandlerNavIdent)
    val besluttersNavn = hentSaksbehandlersNavn(beslutterNavIdent)

    return BrevFørstegangsvedtakInnvilgelseDTO(
        personalia = BrevPersonaliaDTO(
            ident = this.fnr.verdi,
            fornavn = brukersNavn.fornavn,
            etternavn = brukersNavn.mellomnavnOgEtternavn,
            antallBarn = 0,
        ),
        tiltaksnavn = this.behandling.relatertTiltak,
        rammevedtakFraDato = periode.fraOgMed.format(norskDatoFormatter),
        rammevedtakTilDato = periode.tilOgMed.format(norskDatoFormatter),
        saksnummer = saksnummer.verdi,
        barnetillegg = false,
        saksbehandlerNavn = saksbehandlersNavn,
        beslutterNavn = besluttersNavn,
        // TODO post-produksjon: legg inn NORG integrasjon for å hente saksbehandlers kontor.
        kontor = "Nav Tiltak Øst-Viken",
        // Dette er vår dato, det brukes typisk når bruker klager på vedtaksbrev på dato ...
        datoForUtsending = LocalDate.now().format(norskDatoFormatter),
        // TODO pre-mvp jah: Dette må ligge i behandlingen. Men dette vil kunne være en periodisering. Si f.eks. vedtaket går fra slutten av desember.
        sats = 285,
        satsBarn = 53,
    ).let { serialize(it) }
}
