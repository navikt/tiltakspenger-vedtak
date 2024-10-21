package no.nav.tiltakspenger.vedtak.clients.pdfgen

import com.fasterxml.jackson.databind.JsonNode
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn
import no.nav.tiltakspenger.vedtak.routes.objectMapper

private data class DokumentMeldekortDTO(
    val meldekortId: String,
    val sakId: String,
    val meldekortPeriode: PeriodeDTO,
    val saksbehandler: String,
    val meldekortDager: List<MeldekortDagDTO>,
    val tiltakstype: String,
    val iverksattTidspunkt: String,
    val personopplysninger: PersonopplysningerDTO,
) {

    data class PersonopplysningerDTO(
        val fornavn: String,
        val etternavn: String,
        val ident: String,
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

suspend fun Meldekort.UtfyltMeldekort.toPdf(
    hentBrukersNavn: suspend (Fnr) -> Navn,
): JsonNode {
    val navn = hentBrukersNavn(fnr)
    return DokumentMeldekortDTO(
        meldekortId = id.toString(),
        sakId = sakId.toString(),
        meldekortPeriode = DokumentMeldekortDTO.PeriodeDTO(
            fom = periode.fraOgMed.toString(),
            tom = periode.tilOgMed.toString(),
        ),
        saksbehandler = saksbehandler,
        meldekortDager = this.meldeperiode.verdi.map { dag ->
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
        tiltakstype = tiltakstype.toString(),
        iverksattTidspunkt = this.iverksattTidspunkt.toString(),
        personopplysninger = DokumentMeldekortDTO.PersonopplysningerDTO(
            fornavn = navn.fornavn,
            etternavn = navn.mellomnavnOgEtternavn,
            ident = fnr.verdi,
        ),
    ).let { objectMapper.valueToTree(it) }
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
