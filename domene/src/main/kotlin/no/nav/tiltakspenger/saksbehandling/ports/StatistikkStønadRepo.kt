package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkStønadDTO
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkUtbetalingDTO

interface StatistikkStønadRepo {
    fun lagre(dto: StatistikkStønadDTO, context: TransactionContext? = null)
    fun lagre(dto: StatistikkUtbetalingDTO, context: TransactionContext? = null)
}
