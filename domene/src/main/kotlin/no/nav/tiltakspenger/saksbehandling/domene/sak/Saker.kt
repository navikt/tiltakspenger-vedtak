package no.nav.tiltakspenger.saksbehandling.domene.sak

import no.nav.tiltakspenger.libs.periodisering.tilstøter

/**
 * Inneholder en liste over alle saker som er tilgjengelig for en bruker/ident.
 * Garanterer at sakid og  saksnummer er unikt i listen.
 */
data class Saker(
    val ident: String,
    val saker: List<Sak>,
) : List<Sak> by saker {

    init {
        saker.map { it.saksnummer }.also {
            require(it.size == it.toSet().size) { "Saker inneholder duplikate saksnummer: $it" }
        }
        saker.map { it.id }.also {
            require(it.size == it.toSet().size) { "Saker inneholder duplikate IDer: $it" }
        }
        saker.map { it.ident }.also {
            require(it.toSet().size <= 1) { "Oppdaget flere enn 1 ident. En brukers saker må være knyttet til samme ident." }
        }
        saker.map { it.periode }.also {
            require(!it.tilstøter()) { "Saker inneholder tilstøtende eller overlappende perioder: $it" }
        }
        saker.flatMap { sak -> sak.behandlinger.map { it.id } }.also {
            require(it.size == it.toSet().size) { "Saker inneholder duplikate behandlingsid'er: $it" }
        }
        saker.flatMap { sak -> sak.vedtak.map { it.id } }.also {
            require(it.size == it.toSet().size) { "Saker inneholder duplikate vedtaksid'er: $it" }
        }
    }

    fun hentForSaksnummer(saksnummer: Saksnummer): Sak? = saker.find { it.saksnummer == saksnummer }
}
