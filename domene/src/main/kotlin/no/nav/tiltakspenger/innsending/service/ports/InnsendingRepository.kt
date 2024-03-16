package no.nav.tiltakspenger.innsending.service.ports

import no.nav.tiltakspenger.innsending.domene.Innsending

interface InnsendingRepository {
    fun hent(journalpostId: String): Innsending?
    fun lagre(innsending: Innsending): Innsending
    fun findByIdent(ident: String): List<Innsending>
    fun antall(): Long
    fun antallMedTilstandFaktainnhentingFeilet(): Long
    fun antallStoppetUnderBehandling(): Long
    fun hentInnsendingerMedTilstandFaktainnhentingFeilet(): List<String>
    fun hentInnsendingerStoppetUnderBehandling(): List<String>
    fun hentInnsendingerMedTilstandFerdigstilt(): List<String>
}
