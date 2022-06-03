package no.nav.tiltakspenger.domene

import IkkePåKVP
import no.nav.tiltakspenger.domene.fakta.*
import no.nav.tiltakspenger.domene.vilkår.ErOver18År
import no.nav.tiltakspenger.domene.vilkår.IkkePåInstitusjon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VilkårsvurderingTest {
    @Test
    fun `en vilkårsvurdering har utfall IKKE_VURDERT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            fakta = FødselsdatoFakta(),
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        assertTrue(vilkårsvurdering.utfallsperioder.first().utfall == Utfall.IkkeVurdert)
    }

    @Test
    fun `en vilkårsvurdering som mottar riktig fakta har utfall VURDERT_OG_OPPFYLT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            fakta = FødselsdatoFakta(),
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        assertTrue(vilkårsvurdering.utfallsperioder.first().utfall == Utfall.IkkeVurdert)

        val vurderingMedUtfall =
            vilkårsvurdering.vurder(FødselsDatoSystem(fødselsdato = 12.april(2019)))
        assertTrue(vurderingMedUtfall.utfallsperioder.first().utfall == Utfall.VurdertOgOppfylt)
    }

    @Test
    fun `en vilkårsvurdering om KVP hvor bruker sier hen går på det skal til manuell behandling`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = IkkePåKVP,
            fakta = KVPFakta(),
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        val vurderingMedUtfall =
            vilkårsvurdering.vurder(KVPFaktumBruker(deltarKVP = true))
        assertTrue(vurderingMedUtfall.utfallsperioder.first().utfall == Utfall.VurdertOgTrengerManuellBehandling)
    }

    @Test
    fun `en KVP vilkårsvurdering med fakta fra bruker og fakta fra saksbehandler skal avgjøres ved holmgang`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = IkkePåKVP,
            fakta = KVPFakta(),
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(KVPFaktumBruker(deltarKVP = true))
            .vurder(KVPFaktumSaksbehandler(deltarKVP = false))
        assertTrue(vurderingMedUtfall.utfallsperioder.first().utfall == Utfall.VurdertOgOppfylt)
    }


    // | Alt ok | fengsel | Alt Ok |
    @Test
    fun `en bruker bor på institusjon i deler av vurderingsperioden`() {

        //  |--fengsel--|--Ok--|
        val start = 1.mars(2022)
        val om1Uke = start.plusWeeks(1)
        val om2Uker = start.plusWeeks(2)

        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = IkkePåInstitusjon,
            fakta = InstitusjonsoppholdsFakta(),
            vurderingsperiode = Periode(fra = start, til = om2Uker)
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(
                InstitusjonsoppholdsFaktumBruker(
                    opphold = true,
                    oppholdsperiode = Periode(fra = start, til = om1Uke),
                    friKostOgLosji = false
                )
            )

        assertEquals(2, vurderingMedUtfall.utfallsperioder.size)
        assertTrue(vurderingMedUtfall.utfallsperioder[0].utfall == Utfall.VurdertOgIkkeOppfylt)
        assertTrue(vurderingMedUtfall.utfallsperioder[1].utfall == Utfall.VurdertOgOppfylt)
        vurderingMedUtfall.utfallsperioder.forEach {
            when (it.utfall) {
                Utfall.VurdertOgIkkeOppfylt -> {
                    assertEquals(start, it.periode.fra)
                    assertEquals(om1Uke, it.periode.til)
                }
                Utfall.VurdertOgOppfylt -> {
                    assertEquals(om1Uke.plusDays(1), it.periode.fra)
                    assertEquals(om2Uker, it.periode.til)
                }
                else -> fail()
            }
        }
    }

    @Test
    fun `en vurderingsperiode med flere vilkår`() {
        val vilkårsvurdering1 = Vilkårsvurdering(
            vilkår = IkkePåInstitusjon,
            fakta = InstitusjonsoppholdsFakta(),
            vurderingsperiode = Periode(fra = 1.mars(2022), til = 15.mars(2022)),
            utfallsperioder = listOf(
                Utfallsperiode(utfall=Utfall.VurdertOgOppfylt, Periode(fra = 1.mars(2022), til = 3.mars(2022))),
                Utfallsperiode(utfall=Utfall.VurdertOgOppfylt, Periode(fra = 10.mars(2022), til = 15.mars(2022)))
            )
        )


        val vilkårsvurdering2 = Vilkårsvurdering(
            vilkår = IkkePåInstitusjon,
            fakta = InstitusjonsoppholdsFakta(),
            vurderingsperiode = Periode(fra = 1.mars(2022), til = 15.mars(2022)),
            utfallsperioder = listOf(
                Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, Periode(fra = 1.mars(2022), til = 8.mars(2022))),
                Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, Periode(fra = 12.mars(2022), til = 15.mars(2022)))
            )
        )

        //  OK 1-3 | NOT_OK 4-11 | OK 12-15
        //TODO: Dette hører hjemme på tidslinje?
        /*
        assertEquals(
            listOf(
                Periode(fra = 1.mars(2022), til = 3.mars(2022)),
                Periode(fra = 12.mars(2022), til = 15.mars(2022))
            ),
            listOf(vilkårsvurdering1, vilkårsvurdering2).oppfyltePerioder()
        )

         */
    }

    @Test
    fun `akkumulert vilkår prioriterer saksbehandlers faktum`() {
        val periode = Periode(fra = 1.mars(2022), til = 15.mars(2022))
        val saksbehandlerSierOppfylt = Vilkårsvurdering(
            vilkår = IkkePåKVP,
            fakta = KVPFakta(),
            vurderingsperiode = periode,
            utfallsperioder = emptyList()
        )
            .vurder(KVPFaktumBruker(deltarKVP = true))
            .vurder(KVPFaktumSaksbehandler(deltarKVP = false))

        val saksbehandlerSierIkkeOppfylt = Vilkårsvurdering(
            vilkår = IkkePåKVP,
            fakta = KVPFakta(),
            vurderingsperiode = periode,
            utfallsperioder = emptyList()
        )
            .vurder(KVPFaktumSaksbehandler(deltarKVP = true))
            .vurder(KVPFaktumBruker(deltarKVP = false))

        assertEquals(1, saksbehandlerSierOppfylt.utfallsperioder.size)
        assertEquals(periode, saksbehandlerSierOppfylt.utfallsperioder.first().periode)
        assertEquals(Utfall.VurdertOgOppfylt, saksbehandlerSierOppfylt.utfallsperioder.first().utfall)

        assertEquals(1, saksbehandlerSierIkkeOppfylt.utfallsperioder.size)
        assertEquals(periode, saksbehandlerSierIkkeOppfylt.utfallsperioder.first().periode)
        assertEquals(Utfall.VurdertOgIkkeOppfylt, saksbehandlerSierIkkeOppfylt.utfallsperioder.first().utfall)
    }

    @Test
    fun `vilkårsvurdering kan returnere underliggende fakta`() {
        val periode = Periode(fra = 1.mars(2022), til = 15.mars(2022))
        val vurdering = Vilkårsvurdering(
            vilkår = IkkePåKVP,
            fakta = KVPFakta(),
            vurderingsperiode = periode,
            utfallsperioder = emptyList()
        )
            .vurder(KVPFaktumBruker(deltarKVP = true))
            .vurder(KVPFaktumSaksbehandler(deltarKVP = false))

        assertNotNull(vurdering.fakta.saksbehandler)
        assertNotNull(vurdering.fakta.bruker)
        assertNull(vurdering.fakta.system)
    }
}

