package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.Innsending

interface InnsendingRepository {
    fun hent(journalpostId: String): Innsending?
    fun lagre(innsending: Innsending): Innsending
    fun findBySøknadId(søknadId: String): Innsending?
    fun findByIdent(ident: String): List<Innsending>
    fun antall(): Long
    fun antallMedTilstandFaktainnhentingFeilet(): Long
    fun antallStoppetUnderBehandling(): Long
    fun hentInnsendingerMedTilstandFaktainnhentingFeilet(): List<String>
    fun hentInnsendingerStoppetUnderBehandling(): List<String>
}
