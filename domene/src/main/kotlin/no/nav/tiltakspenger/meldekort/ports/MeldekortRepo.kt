package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import java.time.LocalDateTime

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

    fun hentTilBrukerUtfylling(): List<Meldekort>

    fun markerSomSendtTilBrukerUtfylling(meldekortId: MeldekortId, tidspunkt: LocalDateTime)
}
