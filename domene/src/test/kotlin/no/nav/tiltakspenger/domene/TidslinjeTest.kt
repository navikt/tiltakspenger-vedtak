package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.*
import no.nav.tiltakspenger.domene.vilkår.ErOver18År
import no.nav.tiltakspenger.domene.vilkår.IkkePåInstitusjon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TidslinjeTest{
    fun institusjonsoppholdBrukerFaktum(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 10.januar(2022),
    ) = InstitusjonsoppholdsFaktumBruker(
        opphold = true,
        oppholdsperiode = Periode(fra = fra, til = til),
        friKostOgLosji = true,
    )

    fun vilkårsvurderingInstitusjon(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 10.januar(2022),
    ) = Vilkårsvurdering(
        vilkår = IkkePåInstitusjon,
        fakta = InstitusjonsoppholdsFakta(),
        vurderingsperiode = Periode(
            fra = fra,
            til = til,
        ),
    )

    fun vurdertVilkårsvurdering() =
        vilkårsvurderingInstitusjon().vurder(institusjonsoppholdBrukerFaktum())

    @Test
    fun `Tidslinje skal kunne opprettes`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = IkkePåInstitusjon,
            fakta = InstitusjonsoppholdsFakta(),
            vurderingsperiode = Periode(fra = 1.januar(2022), til = 12.januar(2022)),
        ).vurder(
            InstitusjonsoppholdsFaktumBruker(
                opphold = true,
                oppholdsperiode = Periode(fra = 1.januar(2022), til = 10.januar(2022)),
                friKostOgLosji = false
            )
        )
        val vurderinger = listOf(vilkårsvurdering)
        val vilkårsvurderinger = Vilkårsvurderinger(
            periode = Periode(fra = 1.januar(2022), til = 12.januar(2022)),
            vilkårsvurderinger = vurderinger
        )

        val tidslinje = Tidslinje.lagTidslinje(
           vilkårsvurderinger = vilkårsvurderinger
        )

        val (ikkeOppfylteDager, oppfylteDager) = tidslinje.vurderteDager.partition { it.utfallsperiode == Utfall.VurdertOgIkkeOppfylt }
        assertEquals(10, ikkeOppfylteDager.size)
        assertEquals(2, oppfylteDager.size)
        assertEquals(Periode(1.januar(2022), 10.januar(2022)), ikkeOppfylteDager.toPeriode())
        assertEquals(Periode(11.januar(2022), 12.januar(2022)), oppfylteDager.toPeriode())
    }
}