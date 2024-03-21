package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer

interface SakRepo {
    fun hentForIdentMedPeriode(fnr: String, periode: Periode): List<Sak>
    fun lagre(sak: Sak): Sak

    fun hent(sakId: SakId): Sak?
    fun hentSakDetaljer(sakId: SakId): SakDetaljer?
    fun hentForJournalpostId(journalpostId: String): Sak?
    fun hentNesteLøpenr(): String
    fun resetLøpenummer()
}
