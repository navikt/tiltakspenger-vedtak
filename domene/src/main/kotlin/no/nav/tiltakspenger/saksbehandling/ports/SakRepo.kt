package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer

interface SakRepo {
    fun hentForIdent(fnr: Fnr): Saker

    fun hentForSaksnummer(saksnummer: Saksnummer): Sak?

    fun lagre(
        sak: Sak,
        transactionContext: TransactionContext? = null,
    ): Sak

    fun hent(sakId: SakId): Sak?

    fun hentSakDetaljer(sakId: SakId): SakDetaljer?

    fun hentForJournalpostId(journalpostId: String): Sak?

    fun hentNesteSaksnummer(): Saksnummer
}
