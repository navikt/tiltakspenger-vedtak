package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtfallsperioderForVilkårTest {

    @Test
    fun `utfallsperioderForVilkårBuilder skal gi korrekte UtfallsperioderForVilkår`() {
        val utfallsperioderForVilkår = UtfallsperioderForVilkår.utfallsperioderForVilkårBuilder(Over18Vilkår)
            .medUtfallFraOgMedTilOgMed(
                Utfall.VurdertOgOppfylt,
                LocalDate.now().minusWeeks(2),
                LocalDate.now().minusWeeks(1)
            )
            .utvidMedUtfallTilOgMed(Utfall.VurdertOgTrengerManuellBehandling, LocalDate.now())
            .build()
        assertEquals(2, utfallsperioderForVilkår.utfallsperioder.size)
        assertEquals(Over18Vilkår, utfallsperioderForVilkår.vilkår)
        assertEquals(Utfall.VurdertOgOppfylt, utfallsperioderForVilkår.utfallsperioder.first().utfall)
        assertEquals(Utfall.VurdertOgTrengerManuellBehandling, utfallsperioderForVilkår.utfallsperioder.last().utfall)
        assertEquals(
            Periode(LocalDate.now().minusWeeks(2), LocalDate.now().minusWeeks(1)),
            utfallsperioderForVilkår.utfallsperioder.first().periode
        )
        assertEquals(
            Periode(LocalDate.now().minusDays(6), LocalDate.now()),
            utfallsperioderForVilkår.utfallsperioder.last().periode
        )

    }
}
