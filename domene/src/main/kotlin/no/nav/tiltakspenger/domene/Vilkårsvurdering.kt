package no.nav.tiltakspenger.domene

import java.time.LocalDate
import java.time.Month

fun Int.januar(year: Int): LocalDate = LocalDate.of(year, Month.JANUARY, this)
fun Int.februar(year: Int): LocalDate = LocalDate.of(year, Month.FEBRUARY, this)
fun Int.mars(year: Int): LocalDate = LocalDate.of(year, Month.MARCH, this)
fun Int.april(year: Int): LocalDate = LocalDate.of(year, Month.APRIL, this)
fun Int.mai(year: Int): LocalDate = LocalDate.of(year, Month.MAY, this)
fun Int.juni(year: Int): LocalDate = LocalDate.of(year, Month.JUNE, this)
fun Int.juli(year: Int): LocalDate = LocalDate.of(year, Month.JULY, this)
fun Int.august(year: Int): LocalDate = LocalDate.of(year, Month.AUGUST, this)
fun Int.september(year: Int): LocalDate = LocalDate.of(year, Month.SEPTEMBER, this)
fun Int.oktober(year: Int): LocalDate = LocalDate.of(year, Month.OCTOBER, this)
fun Int.november(year: Int): LocalDate = LocalDate.of(year, Month.NOVEMBER, this)
fun Int.desember(year: Int): LocalDate = LocalDate.of(year, Month.DECEMBER, this)

enum class Utfall {
    IKKE_VURDERT,
    VURDERT_OG_OPPFYLT,
    VURDERT_OG_IKKE_OPPFYLT,
    VURDERT_OG_TRENGER_MANUELL_VURDERING
}

data class Vilkårsvurdering(
    val utfall: Utfall = Utfall.IKKE_VURDERT,
    val vilkår: Vilkår,
    val fakta: List<Faktum> = emptyList()
) {
    fun vurder(faktum: Faktum): Vilkårsvurdering {
        val oppdaterteFakta = fakta +faktum //+ listOf(faktum).filter { faktum -> faktum.erRelevantFor(vilkår) }
        return this.copy(
            utfall = vilkår.vurder(oppdaterteFakta),
            fakta = oppdaterteFakta,
        )
    }

}

fun List<Vilkårsvurdering>.erInngangsVilkårOppfylt(): Boolean = this
    .filter { it.vilkår.erInngangsVilkår }
    .all { it.utfall == Utfall.VURDERT_OG_OPPFYLT }
