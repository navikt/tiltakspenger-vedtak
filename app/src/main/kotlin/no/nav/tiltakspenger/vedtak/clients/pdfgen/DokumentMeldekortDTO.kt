package no.nav.tiltakspenger.vedtak.clients.pdfgen

import com.fasterxml.jackson.databind.JsonNode
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.routes.objectMapper

private data class DokumentMeldekortDTO(
    val meldekortId: String,
    val sakId: String,
    val meldekortPeriode: PeriodeDTO,
    val saksbehandler: SaksbehandlerDTO,
    val beslutter: SaksbehandlerDTO,
    val meldekortDager: List<MeldekortDagDTO>,
    val tiltakstype: String,
    val iverksattTidspunkt: String,
    val fødselsnummer: String,
) {

    data class SaksbehandlerDTO(
        val navn: String,
        val navIdent: String,
    )

    data class PeriodeDTO(
        val fom: String,
        val tom: String,
    )

    data class MeldekortDagDTO(
        val dato: String,
        val tiltakType: String,
        val status: String,
        val beløp: Int,
        val prosent: Int,
        val reduksjon: String?,
    )
}

suspend fun Utbetalingsvedtak.toPdf(
    hentSaksbehandlersNavn: suspend (String) -> String,
): JsonNode {
    requireNotNull(beslutter) { "Meldekort som skal journalføres må ha en beslutter. MeldekortId: $id" }

    return DokumentMeldekortDTO(
        fødselsnummer = fnr.verdi,
        saksbehandler = tilSaksbehadlerDto(saksbehandler, hentSaksbehandlersNavn),
        beslutter = tilSaksbehadlerDto(beslutter, hentSaksbehandlersNavn),
        meldekortId = meldekortId.toString(),
        sakId = sakId.toString(),
        meldekortPeriode = DokumentMeldekortDTO.PeriodeDTO(
            fom = periode.fraOgMed.toString(),
            tom = periode.tilOgMed.toString(),
        ),
        meldekortDager = meldekort.meldeperiode.verdi.map { dag ->
            DokumentMeldekortDTO.MeldekortDagDTO(
                dato = dag.dato.toString(),
                tiltakType = dag.tiltakstype.toString(),
                status = dag.toStatus(),
                beløp = dag.beløp,
                prosent = dag.prosent,
                reduksjon = dag.toReduksjon(),
            )
        },
        // TODO pre-mvp jah: Holder det med tiltakstype? Hva bør vi mappe den til?
        tiltakstype = meldekort.tiltakstype.toString(),
        iverksattTidspunkt = vedtakstidspunkt.toString(),
    ).let { objectMapper.valueToTree(it) }
}

private suspend fun tilSaksbehadlerDto(navIdent: String, hentSaksbehandlersNavn: suspend (String) -> String): DokumentMeldekortDTO.SaksbehandlerDTO {
    return DokumentMeldekortDTO.SaksbehandlerDTO(navn = hentSaksbehandlersNavn(navIdent), navIdent = navIdent)
}

private fun Meldekortdag.toStatus(): String {
    return when (this) {
        is Meldekortdag.IkkeUtfylt -> "IkkeUtfylt"
        is Meldekortdag.Utfylt.Deltatt.DeltattMedLønnITiltaket -> "DeltattMedLønnITiltaket"
        is Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket -> "DeltattUtenLønnITiltaket"
        is Meldekortdag.Utfylt.Fravær.Syk.SykBruker -> "SykBruker"
        is Meldekortdag.Utfylt.Fravær.Syk.SyktBarn -> "SyktBarn"
        is Meldekortdag.Utfylt.Fravær.Velferd.VelferdGodkjentAvNav -> "VelferdGodkjentAvNav"
        is Meldekortdag.Utfylt.Fravær.Velferd.VelferdIkkeGodkjentAvNav -> "VelferdIkkeGodkjentAvNav"
        is Meldekortdag.Utfylt.IkkeDeltatt -> "IkkeDeltatt"
        is Meldekortdag.Utfylt.Sperret -> "Sperret"
    }
}

private fun Meldekortdag.toReduksjon(): String? {
    return when (reduksjon) {
        ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon -> "IngenReduksjon"
        ReduksjonAvYtelsePåGrunnAvFravær.Reduksjon -> "Reduksjon"
        ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort -> "YtelsenFallerBort"
        null -> null
    }
}
