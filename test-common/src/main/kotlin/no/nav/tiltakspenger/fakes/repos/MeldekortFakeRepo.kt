package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortSammendrag
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

class MeldekortFakeRepo : MeldekortRepo {
    private val data = Atomic(mutableMapOf<MeldekortId, Meldekort>())

    override fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext?,
    ) {
        data.get()[meldekort.id] = meldekort
    }

    override fun oppdater(
        meldekort: UtfyltMeldekort,
        transactionContext: TransactionContext?,
    ) {
        lagre(meldekort, transactionContext)
    }

    override fun hentForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): Meldekort? = data.get()[meldekortId]

    override fun hentForSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): Meldeperioder? =
        data
            .get()
            .values
            .filter { it.sakId == sakId }
            .let { meldekort ->
                meldekort.firstOrNull()?.let {
                    Meldeperioder(it.tiltakstype, meldekort)
                }
            }

    override fun hentSammendragforSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): List<MeldekortSammendrag> =
        data
            .get()
            .values
            .filter { it.sakId == sakId }
            .map { MeldekortSammendrag(it.id, it.periode, it.status, it.saksbehandler, it.beslutter) }

    override fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): Fnr? = data.get()[meldekortId]?.fnr

    fun hentAlle(): List<UtfyltMeldekort> = data.get().values.filterIsInstance<UtfyltMeldekort>()
}
