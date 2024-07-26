package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import java.time.LocalDate

data class AntallDagerDTO(
    val antallDager: Int,
    val periode: PeriodeDTO,
    val kilde: String,
) {
    fun toPeriodeMedAntallDager(saksbehandlerIdent: String): PeriodeMedVerdi<AntallDager> {
        return PeriodeMedVerdi(
            periode = Periode(fraOgMed = LocalDate.parse(periode.fraOgMed), tilOgMed = LocalDate.parse(periode.tilOgMed)),
            verdi = AntallDager(
                antallDager = antallDager,
                kilde = Kilde.valueOf(kilde.uppercase()),
                saksbehandlerIdent = saksbehandlerIdent,
            ),
        )
    }
}
