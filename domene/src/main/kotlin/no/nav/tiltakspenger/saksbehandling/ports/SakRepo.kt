package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer

interface SakRepo {
    fun hentForIdent(fnr: String): Saker
    fun hentForSaksnummer(saksnummer: String): Sak?
    fun lagre(sak: Sak): Sak
    fun hent(sakId: SakId): Sak?
    fun hentSakDetaljer(sakId: SakId): SakDetaljer?
    fun hentForJournalpostId(journalpostId: String): Sak?
    fun hentNesteSaksnummer(): Saksnummer
}
