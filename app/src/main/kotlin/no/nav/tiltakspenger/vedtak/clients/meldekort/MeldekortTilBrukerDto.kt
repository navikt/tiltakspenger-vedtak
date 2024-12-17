package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortStatus
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.IkkeUtfylt
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattMedLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SykBruker
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SyktBarn
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdIkkeGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.IkkeDeltatt
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Sperret
import java.time.LocalDate

// TODO abn: Dette er veldig work in progress
// Bør inn i felles libs når vi får landet en modell, brukes også i meldekort-api

enum class MeldekortStatusTilBruker {
    KAN_UTFYLLES,
    KAN_IKKE_UTFYLLES,
    GODKJENT,
}

enum class MeldekortDagStatusTilBruker {
    RETT,
    IKKE_RETT,
}

enum class MeldekortDagRapportering {
    DELTATT_UTEN_LØNN,
    DELTATT_MED_LØNN,
    FRAVÆR_SYK,
    FRAVÆR_SYKT_BARN,
    FRAVÆR_ANNET_MED_RETT,
    FRAVÆR_ANNET_UTEN_RETT,
    IKKE_DELTATT,
    IKKE_REGISTRERT,
}

data class MeldekortDagTilBrukerDTO(
    val dag: LocalDate,
    val status: MeldekortDagStatusTilBruker,
    val rapportering: MeldekortDagRapportering,
)

data class MeldekortTilBrukerDTO(
    val id: String,
    val fnr: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val status: MeldekortStatusTilBruker,
    val meldekortDager: List<MeldekortDagTilBrukerDTO>,
)

fun Meldekort.tilBrukerDTO(): MeldekortTilBrukerDTO {
    return MeldekortTilBrukerDTO(
        id = this.id.toString(),
        fnr = this.fnr.verdi,
        fraOgMed = this.fraOgMed,
        tilOgMed = this.tilOgMed,
        status = this.tilBrukerStatusDTO(),
        meldekortDager = this.meldeperiode.dager.map {
            MeldekortDagTilBrukerDTO(
                dag = it.dato,
                status = it.tilBrukerStatusDTO(),
                rapportering = it.tilRapporteringDTO(),
            )
        },
    )
}

fun Meldekort.tilBrukerStatusDTO(): MeldekortStatusTilBruker =
    when (this) {
        is Meldekort.IkkeUtfyltMeldekort -> if (this.erKlarTilUtfylling()) MeldekortStatusTilBruker.KAN_UTFYLLES else MeldekortStatusTilBruker.KAN_IKKE_UTFYLLES
        is Meldekort.UtfyltMeldekort -> when (this.status) {
            MeldekortStatus.GODKJENT -> MeldekortStatusTilBruker.GODKJENT
            else -> MeldekortStatusTilBruker.KAN_IKKE_UTFYLLES
        }
    }

fun Meldekortdag.tilBrukerStatusDTO(): MeldekortDagStatusTilBruker =
    when (this) {
        is IkkeUtfylt -> MeldekortDagStatusTilBruker.RETT
        is Sperret -> MeldekortDagStatusTilBruker.IKKE_RETT
        else -> throw RuntimeException("Oh noes")
    }

fun Meldekortdag.tilRapporteringDTO(): MeldekortDagRapportering =
    when (this) {
        is DeltattMedLønnITiltaket -> MeldekortDagRapportering.DELTATT_MED_LØNN
        is DeltattUtenLønnITiltaket -> MeldekortDagRapportering.DELTATT_UTEN_LØNN
        is SykBruker -> MeldekortDagRapportering.FRAVÆR_SYK
        is SyktBarn -> MeldekortDagRapportering.FRAVÆR_SYKT_BARN
        is VelferdGodkjentAvNav -> MeldekortDagRapportering.FRAVÆR_ANNET_MED_RETT
        is VelferdIkkeGodkjentAvNav -> MeldekortDagRapportering.FRAVÆR_ANNET_UTEN_RETT
        is IkkeDeltatt -> MeldekortDagRapportering.IKKE_DELTATT
        is IkkeUtfylt -> MeldekortDagRapportering.IKKE_REGISTRERT
        is Sperret -> MeldekortDagRapportering.IKKE_REGISTRERT
    }
