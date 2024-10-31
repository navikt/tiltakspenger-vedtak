package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort

interface MeldekortRepo {
    fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext? = null,
    )

    fun oppdater(
        meldekort: Meldekort.UtfyltMeldekort,
        transactionContext: TransactionContext? = null,
    )
}
