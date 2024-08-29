package no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad

import java.time.LocalDate

data class StatistikkUtbetalingDTO(
    val id: String,
    val sakId: String,
    val saksnummer: String,
    val beløp: Int,
    val beløpBeskrivelse: String,
    val årsak: String,
    val posteringDato: LocalDate,
    val gyldigFraDatoPostering: LocalDate,
    val gyldigTilDatoPostering: LocalDate,
)
