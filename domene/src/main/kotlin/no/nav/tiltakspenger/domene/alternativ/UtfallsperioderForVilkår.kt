package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import java.time.LocalDate

class UtfallsperioderForVilkår(val vilkår: Vilkår, val utfallsperioder: List<Utfallsperiode>) {
    companion object {
        fun utfallsperioderForVilkårBuilder(vilkår: Vilkår): UtfallsperiodeBuilder.VilkårUtfallsperiodeBuilder =
            UtfallsperiodeBuilder.VilkårUtfallsperiodeBuilder(vilkår)

        sealed class UtfallsperiodeBuilder {
            class VilkårUtfallsperiodeBuilder(private val vilkår: Vilkår) {
                fun medUtfallFraOgMedTilOgMed(utfall: Utfall, fraOgMed: LocalDate, tilOgMed: LocalDate) =
                    UtvidbarUtfallsperiodeBuilder(vilkår, listOf(Utfallsperiode(utfall, Periode(fraOgMed, tilOgMed))))

                fun medUtfallFraOgMedTilOgMed(utfall: Utfall, periode: Periode) =
                    UtvidbarUtfallsperiodeBuilder(vilkår, listOf(Utfallsperiode(utfall, periode)))
            }

            class UtvidbarUtfallsperiodeBuilder(
                private val vilkår: Vilkår,
                private val utfallsperioder: List<Utfallsperiode>
            ) {
                fun utvidMedUtfallTilOgMed(utfall: Utfall, tilOgMed: LocalDate): UtvidbarUtfallsperiodeBuilder {
                    require(tilOgMed.isAfter(utfallsperioder.last().periode.til))
                    return UtvidbarUtfallsperiodeBuilder(
                        vilkår,
                        utfallsperioder + Utfallsperiode(
                            utfall,
                            Periode(utfallsperioder.last().periode.til.plusDays(1), tilOgMed)
                        )
                    )
                }

                fun build() = UtfallsperioderForVilkår(vilkår, utfallsperioder)
            }
        }
    }
}