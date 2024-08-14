package no.nav.tiltakspenger.vedtak.repository.statistikk.sak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.StatistikkSakDTO

interface StatistikkSakDAO {
    fun lagre(dto: StatistikkSakDTO, tx: TransactionalSession)
}
