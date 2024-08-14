package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkStønadDTO

interface StatistikkStønadRepo {
    fun lagre(dto: StatistikkStønadDTO, context: TransactionContext? = null)
}
