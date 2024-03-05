package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SakDetaljer
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

interface SakRepo {
    fun hentForIdentMedPeriode(fnr: String, periode: Periode): List<Sak>
    fun lagre(sak: Sak): Sak

    fun hent(sakId: SakId): Sak?
    fun hentSakDetaljer(sakId: SakId): SakDetaljer?
    fun hentForJournalpostId(journalpostId: String): Sak?
}
