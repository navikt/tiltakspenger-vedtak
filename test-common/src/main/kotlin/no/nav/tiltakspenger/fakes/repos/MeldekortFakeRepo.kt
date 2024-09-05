package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortSammendrag
import no.nav.tiltakspenger.meldekort.domene.Meldekortperioder
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

class MeldekortFakeRepo : MeldekortRepo {

    private val data = Atomic(mutableMapOf<MeldekortId, Meldekort>())

    override fun lagre(meldekort: Meldekort, transactionContext: TransactionContext?) {
        data.get()[meldekort.id] = meldekort
    }

    override fun oppdater(meldekort: Meldekort, transactionContext: TransactionContext?) {
        lagre(meldekort, transactionContext)
    }

    override fun hentForMeldekortId(meldekortId: MeldekortId, sessionContext: SessionContext?): Meldekort? {
        return data.get()[meldekortId]
    }

    override fun hentforSakId(sakId: SakId, sessionContext: SessionContext?): Meldekortperioder? {
        return data.get().values
            .filter { it.sakId == sakId }.let { meldekort ->
                meldekort.firstOrNull()?.let {
                    Meldekortperioder(it.tiltakstype, meldekort)
                }
            }
    }

    override fun hentSammendragforSakId(sakId: SakId, sessionContext: SessionContext?): List<MeldekortSammendrag> {
        return data.get().values
            .filter { it.sakId == sakId }
            .map { MeldekortSammendrag(it.id, it.periode, it is Meldekort.UtfyltMeldekort) }
    }

    override fun hentFnrForMeldekortId(meldekortId: MeldekortId, sessionContext: SessionContext?): Fnr? {
        return data.get()[meldekortId]?.fnr
    }
}
