package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO

data class AntallDagerDTO(
    val antallDager: Int,
    val periode: PeriodeDTO,
    val kilde: String,
) {
    fun toPeriodeMedAntallDager(saksbehandlerIdent: String): PeriodeMedVerdi<AntallDager> {
        return PeriodeMedVerdi(
            periode = Periode(fra = periode.fra, til = periode.til),
            verdi = AntallDager(
                antallDager = antallDager,
                kilde = Kilde.valueOf(kilde.uppercase()),
                saksbehandlerIdent = saksbehandlerIdent,
            ),
        )
    }
}
