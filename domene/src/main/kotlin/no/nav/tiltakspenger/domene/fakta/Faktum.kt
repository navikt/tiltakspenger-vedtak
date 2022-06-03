package no.nav.tiltakspenger.domene.fakta

import java.time.LocalDate

interface Faktum {
    val kilde: FaktumKilde
}
interface Fakta<T: Faktum>{
    fun leggTil(faktum: T): Fakta<T>
}

enum class FaktumKilde : Comparable<FaktumKilde> {
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

/*
interface Faktum {
    val kilde: FaktumKilde
}*/



