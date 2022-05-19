package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import no.nav.tiltakspenger.domene.vilkår.Institusjonsopphold
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TidslinjeTest{
    fun institusjonsoppholdBrukerFaktum(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 10.januar(2022),
    ) = InstitusjonsoppholdsFaktum(
        opphold = true,
        kilde = FaktumKilde.BRUKER,
        oppholdsperiode = listOf(Periode(fra = fra, til = til)),
        friKostOgLosji = true,
    )

    fun vilkårsvurderingInstitusjon(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 10.januar(2022),
    ) = Vilkårsvurdering(
        vilkår = Institusjonsopphold,
        vurderingsperiode = Periode(
            fra = fra,
            til = til,
        )
    )

    fun vurdertVilkårsvurdering() =
        vilkårsvurderingInstitusjon().vurder(institusjonsoppholdBrukerFaktum())

    @Test
    fun foo() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = 1.januar(2022), til = 12.januar(2022)),
        ).vurder(
            InstitusjonsoppholdsFaktum(
                opphold = true,
                kilde = FaktumKilde.BRUKER,
                oppholdsperiode = listOf(Periode(fra = 1.januar(2022), til = 10.januar(2022))),
                friKostOgLosji = false
            )
        )
        val tidslinje = Tidslinje(
           vilkårsvurderinger = listOf(vilkårsvurdering)
        )

        assertEquals(1.januar(2022), tidslinje.fra)
        val instDager = tidslinje.dager
            .take(10)
            .filter { it.utfall is Utfall.VurdertOgOppfylt }.size


    }
}