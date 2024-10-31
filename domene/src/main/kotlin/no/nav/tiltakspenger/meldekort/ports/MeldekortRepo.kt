package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder

interface MeldekortRepo {
    fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext? = null,
    )

    fun oppdater(
        meldekort: Meldekort.UtfyltMeldekort,
        transactionContext: TransactionContext? = null,
    )

    fun hentForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext? = null,
    ): Meldekort?

    fun hentForSakId(
        sakId: SakId,
        sessionContext: SessionContext? = null,
    ): Meldeperioder?

    fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext? = null,
    ): Fnr?
}
