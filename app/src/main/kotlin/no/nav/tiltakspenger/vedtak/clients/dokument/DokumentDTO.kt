package no.nav.tiltakspenger.vedtak.clients.dokument

import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattMedLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SykBruker
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SyktBarn
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdIkkeGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.IkkeDeltatt
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Sperret
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.DELTATT_MED_LØNN_I_TILTAKET
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.DELTATT_UTEN_LØNN_I_TILTAKET
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.FRAVÆR_SYK
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.FRAVÆR_SYKT_BARN
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.FRAVÆR_VELFERD_GODKJENT_AV_NAV
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.IKKE_DELTATT
import no.nav.tiltakspenger.vedtak.clients.dokument.MeldekortDagStatusTilDokumentDTO.SPERRET
import java.time.LocalDate
import java.time.LocalDateTime

internal data class DokumentMeldekortDTO(
    val meldekortId: String,
    val sakId: String,
    val meldekortPeriode: PeriodeDTO,
    val saksbehandler: String,
    val meldekortDager: List<MeldekortDagDTO>,
    val innsendingTidspunkt: LocalDateTime,
    val fnr: String,
)

// TODO jah: Ikke viktig, men denne kan flyttes til en felles plass i libs også kan vi slette kopiene overalt. Da kan den og ha tilDomene() eller tilPeriode()
internal data class PeriodeDTO(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)

internal data class MeldekortDagDTO(
    val dato: LocalDate,
    val tiltakType: TiltakstypeSomGirRett?,
    val status: MeldekortDagStatusTilDokumentDTO,
)

internal enum class MeldekortDagStatusTilDokumentDTO {
    SPERRET,
    DELTATT_UTEN_LØNN_I_TILTAKET,
    DELTATT_MED_LØNN_I_TILTAKET,
    IKKE_DELTATT,
    FRAVÆR_SYK,
    FRAVÆR_SYKT_BARN,
    FRAVÆR_VELFERD_GODKJENT_AV_NAV,
    FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV,
}

internal fun mapMeldekortDTOTilDokumentDTO(
    vedtak: Utbetalingsvedtak,
): DokumentMeldekortDTO =
    DokumentMeldekortDTO(
        meldekortId = vedtak.meldekortId.toString(),
        meldekortPeriode = PeriodeDTO(vedtak.periode.fraOgMed, vedtak.periode.tilOgMed),
        sakId = vedtak.sakId.toString(),
        saksbehandler = vedtak.saksbehandler,
        meldekortDager =
        vedtak.meldekortperiode.verdi.map { dag ->
            MeldekortDagDTO(
                dato = dag.dato,
                tiltakType = dag.tiltakstype,
                status =
                when (dag) {
                    is DeltattMedLønnITiltaket -> DELTATT_MED_LØNN_I_TILTAKET
                    is DeltattUtenLønnITiltaket -> DELTATT_UTEN_LØNN_I_TILTAKET
                    is SykBruker -> FRAVÆR_SYK
                    is SyktBarn -> FRAVÆR_SYKT_BARN
                    is VelferdGodkjentAvNav -> FRAVÆR_VELFERD_GODKJENT_AV_NAV
                    is VelferdIkkeGodkjentAvNav -> FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
                    is IkkeDeltatt -> IKKE_DELTATT
                    is Sperret -> SPERRET
                },
            )
        },
        innsendingTidspunkt = vedtak.vedtakstidspunkt,
        fnr = vedtak.fnr.verdi,
    )
