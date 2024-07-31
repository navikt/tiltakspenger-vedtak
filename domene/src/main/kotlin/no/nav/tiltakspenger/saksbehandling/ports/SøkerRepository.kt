package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker

interface SøkerRepository {
    fun findByIdent(fnr: Fnr, sessionContext: SessionContext? = null): Søker?
    fun hent(søkerId: SøkerId): Søker?
    fun lagre(søker: Søker, transactionContext: TransactionContext? = null)
}
