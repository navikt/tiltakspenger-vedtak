package no.nav.tiltakspenger.vedtak.clients.utbetaling

interface Utbetaling {
    suspend fun iverksett(utbetalingDTO: UtbetalingDTO): String
}
