package no.nav.tiltakspenger.vedtak.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsperiode
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import java.time.LocalDate

internal data class UtbetalingsvedtakDTO(
    val id: String,
    val periode: PeriodeDTO,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
    val antallBarn: Int,
    val totalbeløp: Int,
    val vedtakDager: List<DagDTO>,
) {
    data class DagDTO(
        val beløp: Int,
        val dato: LocalDate,
        val tiltakType: String,
        val status: String,
    )
}

internal fun mapVedtak(vedtak: Utbetalingsvedtak): UtbetalingsvedtakDTO {
    val sats = vedtak.utbetalingsperiode.satsUnsafe()
    return UtbetalingsvedtakDTO(
        id = vedtak.id.toString(),
        periode = vedtak.periode.toDTO(),
        sats = sats.sats,
        satsDelvis = sats.satsDelvis,
        satsBarnetillegg = sats.satsBarnetillegg,
        satsBarnetilleggDelvis = sats.satsBarnetilleggDelvis,
        antallBarn = 0,
        totalbeløp = vedtak.beløp,
        vedtakDager =
        vedtak.utbetalingsperiode.flatMap {
            when (it) {
                is Utbetalingsperiode.SkalUtbetale -> it.map { dag ->
                    UtbetalingsvedtakDTO.DagDTO(
                        beløp = dag.beløp,
                        dato = dag.dato,
                        tiltakType = dag.tiltakstype.name,
                        status = dag.status.name,
                    )
                }

                is Utbetalingsperiode.SkalIkkeUtbetale -> it.periode.tilDager().map { dag ->
                    UtbetalingsvedtakDTO.DagDTO(
                        beløp = 0,
                        dato = dag,
                        // TODO pre-mvp jah: Redefiner kontrakten med frontend
                        tiltakType = "IKKE_TILTAK",
                        status = "IKKE_UTBETALT",
                    )
                }
            }
        },
    )
}
