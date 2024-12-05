package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort

interface MeldekortRepo {

    fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext? = null,
    )

    /**
     * TODO jah: Slå sammen lagre og oppdater til en metode.
     */
    fun oppdater(
        meldekort: Meldekort,
        transactionContext: TransactionContext? = null,
    )
}
