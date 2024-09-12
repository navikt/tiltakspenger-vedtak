package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.domene.KanIkkeIverksetteMeldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService

class IverksettMeldekortService(
    val sakService: SakService,
    val meldekortRepo: MeldekortRepo,
    val sessionFactory: SessionFactory,
) {
    fun iverksettMeldekort(
        kommando: IverksettMeldekortKommando,
    ): Either<KanIkkeIverksetteMeldekort, Meldekort.UtfyltMeldekort> {
        val meldekortId = kommando.meldekortId
        val sakId = kommando.sakId
        val sak = sakService.hentForSakId(sakId, kommando.beslutter)
            ?: throw IllegalArgumentException("Fant ikke sak med id $sakId")
        val meldekort: Meldekort = sak.hentMeldekort(meldekortId)
            ?: throw IllegalArgumentException("Fant ikke meldekort med id $meldekortId i sak $sakId")
        meldekort as Meldekort.UtfyltMeldekort

        val rammevedtak = sak.hentRammevedtak()
            ?: throw IllegalArgumentException("Fant ikke rammevedtak for sak $sakId")

        return meldekort.iverksettMeldekort(kommando.beslutter)
            .onRight { iverksattMeldekort ->
                val nesteMeldekort = meldekort.opprettNesteMeldekort(rammevedtak.utfallsperioder)
                sessionFactory.withTransactionContext { tx ->
                    meldekortRepo.oppdater(iverksattMeldekort, tx)
                    nesteMeldekort.onRight {
                        meldekortRepo.lagre(it, tx)
                    }
                }
            }
    }
}
