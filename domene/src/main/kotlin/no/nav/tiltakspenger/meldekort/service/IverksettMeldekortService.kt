package no.nav.tiltakspenger.meldekort.service

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo

class IverksettMeldekortService(
    val hentMeldekortService: HentMeldekortService,
    val meldekortRepo: MeldekortRepo,
    val sessionFactory: SessionFactory,
    val rammevedtakRepo: RammevedtakRepo,
) {
    fun iverksettMeldekort(kommando: IverksettMeldekortKommando): Meldekort.UtfyltMeldekort {
        val meldekortId = kommando.meldekortId
        val meldekort: Meldekort =
            hentMeldekortService.hentMeldekort(meldekortId, kommando.beslutter)
                ?: throw IllegalStateException("Fant ikke meldekort med id $meldekortId")
        // TODO pre-mvp: Sjekk at beslutter har rollen beslutter. Lag en hent-funksjon i en service som sjekker kode6/7/skjermet og sjekker roller.
        meldekort as Meldekort.UtfyltMeldekort

        val rammevedtak = rammevedtakRepo.hent(meldekort.rammevedtakId)!!

        return meldekort.iverksettMeldekort(kommando.beslutter).also { iverksattMeldekort ->
            val nesteMeldekort = meldekort.opprettNesteMeldekort(rammevedtak.utfallsperioder)
            sessionFactory.withTransactionContext { tx ->
                meldekortRepo.lagre(iverksattMeldekort, tx)
                nesteMeldekort.onRight {
                    meldekortRepo.lagre(it, tx)
                }
            }
        }
    }
}
