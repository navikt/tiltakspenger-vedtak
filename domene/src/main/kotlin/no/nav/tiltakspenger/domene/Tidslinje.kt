package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Tidslinje private constructor(
    val fra: LocalDate,
    val til: LocalDate,
//    val dager: List<VurdertDag>,
    val utfallsPeriode: List<VurdertPeriode>,
) {
    constructor(vilkårsvurderinger: List<Vilkårsvurdering>)
        :this(
            fra = vilkårsvurderinger.minOf { it.vurderingsperiode.fra },
            til = vilkårsvurderinger.maxOf { it.vurderingsperiode.til },
//            dager = ,
            utfallsPeriode = emptyList(),
        )
}

class VurdertDag(
    val dag: LocalDate,
    val utfall: Utfall,
    val ikkeOppfylteVilkår: List<Vilkårsvurdering> = emptyList()
) {

}