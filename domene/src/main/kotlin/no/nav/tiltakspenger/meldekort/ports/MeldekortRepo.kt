package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortSammendrag
import no.nav.tiltakspenger.meldekort.domene.Meldekortperioder

interface MeldekortRepo {
    fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext? = null,
    )

    fun hentForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext? = null,
    ): Meldekort?

    fun hentforSakId(
        sakId: SakId,
        sessionContext: SessionContext? = null,
    ): Meldekortperioder?

    fun hentSammendragforSakId(
        sakId: SakId,
        sessionContext: SessionContext? = null,
    ): List<MeldekortSammendrag>

    fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext? = null,
    ): Fnr?

    fun hentUtfylteMeldekortForFørstegangsbehandlingId(førstegangsbehandlingId: BehandlingId): Meldekortperioder?
}
