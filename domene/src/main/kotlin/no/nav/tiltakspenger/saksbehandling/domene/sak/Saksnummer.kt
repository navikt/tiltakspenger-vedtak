package no.nav.tiltakspenger.saksbehandling.domene.sak

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Saksnummer(
    val verdi: String,
) {
    constructor(dato: LocalDate, løpenr: Int) : this(genererSaksnummerPrefiks(dato) + løpenr)

    val prefiks = verdi.substring(0, 8)
    val løpenr = verdi.substring(8).toInt()
    val dato = LocalDate.parse(prefiks, DateTimeFormatter.ofPattern("yyyyMMdd"))

    init {
        require(verdi.length >= 12) { "Saksnummer må være 12 tegn eller lengre" }
        require(løpenr >= 1001) { "Løpenummer må være lik eller større enn 1001" }
    }
    companion object {
        fun genererSaknummer(
            dato: LocalDate = LocalDate.now(),
            løpenr: Int = 1001,
        ): Saksnummer {
            return Saksnummer(genererSaksnummerPrefiks(dato) + løpenr)
        }

        fun genererSaksnummerPrefiks(date: LocalDate): String =
            date.year.toString() + String.format("%02d%02d", date.monthValue, date.dayOfMonth)

        fun nesteSaksnummer(sisteSaksnummer: Saksnummer): Saksnummer {
            val prefiks = sisteSaksnummer.prefiks
            val nesteLøpenummer = sisteSaksnummer.løpenr + 1
            return Saksnummer(prefiks + nesteLøpenummer)
        }
    }
}
