package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.Fakta
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


data class VurdertPeriode<FaktaType, FaktumType>(
    val periode: Periode,
    val samletUtfall: Utfall,
    val vilkårsvurderinger: List<Vilkårsvurdering<FaktaType, FaktumType>> = emptyList()
) where FaktaType: Fakta<FaktumType>, FaktumType: Faktum

fun List<VurdertDag>.toPeriode(): Periode = Periode(
    fra = this.minOf { it.dag },
    til = this.maxOf { it.dag }
)

class VurdertDag(
    val dag: LocalDate,
    val utfallsperiode: Utfall,
    val vilkårsvurderinger: List<Vilkårsvurdering<out Fakta<out Faktum>, out Faktum>> = emptyList()
) {
    fun ikkeOppfylteVilkår(): List<Vilkårsvurdering<out Fakta<out Faktum>, out Faktum>> = emptyList()
    fun oppfylt(): Boolean = false
}
