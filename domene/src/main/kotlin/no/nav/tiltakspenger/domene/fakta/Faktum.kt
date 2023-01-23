package no.nav.tiltakspenger.domene.fakta

import java.time.LocalDate

enum class FaktumKilde {
    BRUKER,
    SYSTEM,
    SAKSBEHANDLER
}

sealed class Faktum2<R>(val tilstand: Tilstand, private val verdi: R?) {
    fun get(): R =
        if (tilstand == Tilstand.UKJENT) throw IllegalArgumentException("Ukjent tilstand har ikke verdi") else verdi!!

    enum class Tilstand {
        KJENT,
        UKJENT
    }
}

class Fødselsdato private constructor(tilstand: Tilstand, private val dato: LocalDate?) :
    Faktum2<LocalDate>(tilstand, dato) {
    constructor() : this(Tilstand.UKJENT, null)
    constructor(date: LocalDate) : this(Tilstand.KJENT, date)
}

// fun main(){
//    val ukjentFødselsdato = Fødselsdato()
//    val kjentfødselsdato = Fødselsdato(LocalDate.now())
//    val listeMedfakta = listOf(ukjentFødselsdato, kjentfødselsdato)
//    val ukjentefakta = listeMedfakta.all { it.tilstand == Faktum2.Tilstand.Ukjent }
// }

interface Faktum {
    val kilde: FaktumKilde
}
