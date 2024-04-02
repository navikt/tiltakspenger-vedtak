package no.nav.tiltakspenger.domene.vilkår.temp

/*
Denne klassen representerer en sammenhengende periode som har samme verdi for hele perioden.
Perioden kan ikke ha "hull" som ikke har en verdi
 */
data class PeriodeMedVerdi<T : Sammenlignbar<T>>(
    val verdi: T,
    val periode: Periode,
)

fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.allePerioderHarSammeVerdi() =
    this.map { it.verdi }.distinct().size <= 1

fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.perioderMedSammeVerdi(verdi: T): List<PeriodeMedVerdi<T>> =
    this.filter { it.verdi == verdi }

fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.perioderMedUlikVerdi(verdi: T): List<PeriodeMedVerdi<T>> =
    this.filter { it.verdi != verdi }

fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.trekkFra(periode: Periode): List<PeriodeMedVerdi<T>> =
    this.flatMap {
        it.periode.trekkFra(listOf(periode))
            .map { nyPeriode -> PeriodeMedVerdi(it.verdi, nyPeriode) }
    }
