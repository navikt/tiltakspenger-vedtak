package no.nav.tiltakspenger.vedtak.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class VedtakUtenDagerDTO(
    val id: String,
    val periode: PeriodeDTO,
    val beløp: Int,
)

internal fun mapAlleVedtak(vedtakListe: List<Utbetalingsvedtak>): List<VedtakUtenDagerDTO> =
    vedtakListe.map { vedtak ->
        VedtakUtenDagerDTO(
            id = vedtak.id.toString(),
            periode = vedtak.periode.toDTO(),
            beløp = vedtak.beløp,
        )
    }.sortedByDescending { it.periode.fraOgMed }
