package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.Søker
import java.util.*

data class SøkerDto(
    val id: UUID,
    val ident: String,
    val tilstand: String
) {
    companion object {
        fun fromSøker(søker: Søker) = SøkerDto(søker.id, søker.ident, søker.tilstand.type.toString())
    }
}
