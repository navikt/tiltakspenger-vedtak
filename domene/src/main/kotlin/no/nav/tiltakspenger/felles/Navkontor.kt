package no.nav.tiltakspenger.felles

import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * Også kallt kontornummer/enhetsnummer.
 * Inngang befolkning: https://www.nav.no/sok-nav-kontor eksempel https://www.nav.no/kontor/nav-asker
 * Se også etterlatte sin take på det samme: https://github.com/navikt/pensjon-etterlatte-saksbehandling/blob/main/libs/saksbehandling-common/src/main/kotlin/Enhetsnummer.kt
 */
data class Navkontor(
    val kontornummer: String,
) {
    init {
        require(erGyldig(kontornummer)) { "Forventet at enhetsnummer/kontornummer er 4 siffer, men var: $kontornummer" }
    }

    companion object {
        fun tryCreate(kontornummer: String): Either<UgyldigKontornummer, Navkontor> {
            return if (erGyldig(kontornummer)) {
                Navkontor(kontornummer).right()
            } else {
                UgyldigKontornummer.left()
            }
        }

        private fun erGyldig(input: String): Boolean {
            return input.matches(Regex("^\\d{4}$"))
        }
    }
}

object UgyldigKontornummer
