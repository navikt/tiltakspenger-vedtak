package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.Søker

interface SøkerRepository {
    fun hent(ident: String): Søker?
    fun lagre(søker: Søker): Int
}