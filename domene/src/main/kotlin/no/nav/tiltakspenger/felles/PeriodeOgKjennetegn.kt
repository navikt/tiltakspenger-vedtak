package no.nav.tiltakspenger.felles

interface Kjennetegn

interface PeriodeOgKjennetegn {
    fun kjennetegn(): Kjennetegn
    fun periode(): Periode
}

fun List<PeriodeOgKjennetegn>.sammenhengendePerioderPerKjennetegn() =
    this
        .groupBy { it.kjennetegn() }
        .mapValues { it.value.map { periodeOgKjennetegn -> periodeOgKjennetegn.periode() }.leggSammen() }
