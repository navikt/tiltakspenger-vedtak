package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import java.time.LocalDateTime

class MeldekortFakeRepo : MeldekortRepo {
    private val data = Atomic(mutableMapOf<MeldekortId, Meldekort>())

    override fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext?,
    ) {
        data.get()[meldekort.id] = meldekort
    }

    override fun oppdater(
        meldekort: Meldekort,
        transactionContext: TransactionContext?,
    ) {
        lagre(meldekort, transactionContext)
    }

    override fun hentTilBrukerUtfylling(): List<Meldekort> {
        TODO("Not yet implemented")
    }

    override fun markerSomSendtTilBrukerUtfylling(meldekortId: MeldekortId, tidspunkt: LocalDateTime) {
        TODO("Not yet implemented")
    }

    fun hentForSakId(
        sakId: SakId,
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

    fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
    ): Fnr? = data.get()[meldekortId]?.fnr
}
