package no.nav.tiltakspenger.vedtak.repository.statistikk.sak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.service.statistikk.SakStatistikkDTO

interface SakStatistikkRepoIntern {
    fun lagre(dto: SakStatistikkDTO, tx: TransactionalSession)
}
