package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.sak.Sak
import no.nav.tiltakspenger.saksbehandling.sak.SakDetaljer

interface SakRepo {
    fun hentForIdentMedPeriode(fnr: String, periode: Periode): List<Sak>
    fun lagre(sak: Sak): Sak

    fun hent(sakId: SakId): Sak?
    fun hentSakDetaljer(sakId: SakId): SakDetaljer?
    fun hentForJournalpostId(journalpostId: String): Sak?
}
