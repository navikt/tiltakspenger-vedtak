package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Tidslinje private constructor(
    val fra: LocalDate,
    val til: LocalDate,
    val dager: List<VurdertDag>,
    val utfallsPeriode: List<Periode>,
//    val utfall: List<Utfall>
) {
    constructor(vilkårsvurderinger: List<Vilkårsvurdering>)
        :this(
            fra = vilkårsvurderinger.minOf { it.vurderingsperiode.fra },
            til = vilkårsvurderinger.maxOf { it.vurderingsperiode.til },
            dager = emptyList(),
            utfallsPeriode = emptyList(),
        )
}

class VurdertDag(
    val dag: LocalDate,
    // val utfall: Utfall,
    val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList()
) {

    fun oppfylt(): Boolean =
        vilkårsvurderinger
            .filter { /*Bare riktig dag*/ }
            .filter { it.utfall.any { it is Utfall.VurdertOgOppfylt} }.isEmpty()
}