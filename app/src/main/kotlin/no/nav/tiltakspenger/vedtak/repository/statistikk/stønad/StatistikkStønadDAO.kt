package no.nav.tiltakspenger.vedtak.repository.statistikk.stønad

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkStønadDTO
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkUtbetalingDTO

interface StatistikkStønadDAO {
    fun lagre(dto: StatistikkStønadDTO, tx: TransactionalSession)
    fun lagre(dto: StatistikkUtbetalingDTO, tx: TransactionalSession)
}
