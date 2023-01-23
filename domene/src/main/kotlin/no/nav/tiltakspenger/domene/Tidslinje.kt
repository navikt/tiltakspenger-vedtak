package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Tidslinje private constructor(
    val vurderteDager: List<VurdertDag>
) {
    companion object {
        fun lagTidslinje(vilkårsvurderinger: Vilkårsvurderinger): Tidslinje {
            val dager = vilkårsvurderinger.periode.tilDager().map { dag ->
                val relevanteUtfallsperioder = vilkårsvurderinger.vilkårsvurderinger.mapNotNull { vilkårsvurdering ->
                    vilkårsvurdering.utfallsperioder.find { it.periode.inneholder(dag) }
                }

                val utfall = when {
                    relevanteUtfallsperioder.all { it.utfall == Utfall.VurdertOgOppfylt } -> Utfall.VurdertOgOppfylt
                    relevanteUtfallsperioder.any { it.utfall == Utfall.VurdertOgIkkeOppfylt } -> Utfall.VurdertOgIkkeOppfylt
                    relevanteUtfallsperioder.any { it.utfall == Utfall.VurdertOgTrengerManuellBehandling } -> Utfall.VurdertOgTrengerManuellBehandling
                    else -> Utfall.IkkeVurdert
                }

                VurdertDag(
                    dag = dag,
                    utfallsperiode = utfall
                )
            }
            return Tidslinje(
                vurderteDager = dager
            )
        }
    }
}

data class VurdertPeriode(
    val periode: Periode,
    val samletUtfall: Utfall,
    val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList()
)

fun List<VurdertDag>.toPeriode(): Periode = Periode(
    fra = this.minOf { it.dag },
    til = this.maxOf { it.dag }
)

class VurdertDag(
    val dag: LocalDate,
    val utfallsperiode: Utfall,
    val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList()
) {

    fun ikkeOppfylteVilkår(): List<Vilkårsvurdering> = emptyList()

    fun oppfylt(): Boolean = false
    /*
    vilkårsvurderinger
        .filter { /*Bare riktig dag*/ }
        .filter { it.utfallsperiodes.any { it is Utfallsperiode.VurdertOgOppfylt} }.isEmpty()*/
}
