package no.nav.tiltakspenger.domene

import KVP
import no.nav.tiltakspenger.domene.fakta.FaktumKilde
import no.nav.tiltakspenger.domene.fakta.FødselsdatoFaktum
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import no.nav.tiltakspenger.domene.fakta.KVPFaktum
import no.nav.tiltakspenger.domene.vilkår.ErOver18År
import no.nav.tiltakspenger.domene.vilkår.Institusjonsopphold
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VilkårsvurderingTest {
    @Test
    fun `en vilkårsvurdering har utfall IKKE_VURDERT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        assertTrue(vilkårsvurdering.utfall.first() is Utfall.IkkeVurdert)
    }

    @Test
    fun `en vilkårsvurdering som mottar riktig fakta har utfall VURDERT_OG_OPPFYLT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        assertTrue(vilkårsvurdering.utfall.first() is Utfall.IkkeVurdert)

        val vurderingMedUtfall =
            vilkårsvurdering.vurder(FødselsdatoFaktum(fødselsdato = 12.april(2019), kilde = FaktumKilde.BRUKER))
        assertTrue(vurderingMedUtfall.utfall.first() is Utfall.VurdertOgOppfylt)
    }

    @Test
    fun `en vilkårsvurdering om KVP hvor bruker sier hen går på det skal til manuell behandling`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = KVP,
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        val vurderingMedUtfall =
            vilkårsvurdering.vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
        assertTrue(vurderingMedUtfall.utfall.first() is Utfall.VurdertOgTrengerManuellBehandling)
    }

    @Test
    fun `en KVP vilkårsvurdering med fakta fra bruker og fakta fra saksbehandler skal avgjøres ved holmgang`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = KVP,
            vurderingsperiode = Periode(fra = 13.april(2019), til = 20.april(2019))
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
            .vurder(KVPFaktum(deltarKVP = false, kilde = FaktumKilde.SAKSBEHANDLER))
        assertTrue(vurderingMedUtfall.utfall.first() is Utfall.VurdertOgOppfylt)
    }


    // | Alt ok | fengsel | Alt Ok |
    @Test
    fun `en bruker bor på institusjon i deler av vurderingsperioden`() {

        //  |--fengsel--|--Ok--|
        val start = 1.mars(2022)
        val om1Uke = start.plusWeeks(1)
        val om2Uker = start.plusWeeks(2)

        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = start, til = om2Uker)
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(
                InstitusjonsoppholdsFaktum(
                    opphold = true,
                    kilde = FaktumKilde.BRUKER,
                    oppholdsperiode = listOf(Periode(fra = start, til = om1Uke)),
                    friKostOgLosji = false
                )
            )

        assertEquals(2, vurderingMedUtfall.utfall.size)
        assertTrue(vurderingMedUtfall.utfall[0] is Utfall.VurdertOgIkkeOppfylt)
        assertTrue(vurderingMedUtfall.utfall[1] is Utfall.VurdertOgOppfylt)
        vurderingMedUtfall.utfall.forEach {
            when (it) {
                is Utfall.VurdertOgIkkeOppfylt -> {
                    assertEquals(start, it.periode.fra)
                    assertEquals(om1Uke, it.periode.til)
                }
                is Utfall.VurdertOgOppfylt -> {
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
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = 1.mars(2022), til = 15.mars(2022)),
            utfall = listOf(
                Utfall.VurdertOgOppfylt(Periode(fra = 1.mars(2022), til = 3.mars(2022))),
                Utfall.VurdertOgOppfylt(Periode(fra = 10.mars(2022), til = 15.mars(2022)))
            )
        )


        val vilkårsvurdering2 = Vilkårsvurdering(
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = 1.mars(2022), til = 15.mars(2022)),
            utfall = listOf(
                Utfall.VurdertOgOppfylt(Periode(fra = 1.mars(2022), til = 8.mars(2022))),
                Utfall.VurdertOgOppfylt(Periode(fra = 12.mars(2022), til = 15.mars(2022)))
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
}

/*
fun List<Vilkårsvurdering>.oppfyltePerioder(): List<Periode> {
    if (this.isEmpty()) return emptyList()
    val vurderingsPeriode = this.first().vurderingsperiode
    return this
        .filter { it.utfall.first() is Utfall.VurdertOgOppfylt }
        .map { it.utfall as Utfall.VurdertOgOppfylt }
        .fold(listOf(vurderingsPeriode)) { fratrektVurderingsPeriode, vurdertOgOppfylt ->
            val ikkeOppfyltPerioder = vurderingsPeriode.ikkeOverlappendePerioder(vurdertOgOppfylt.perioder)
            return@fold fratrektVurderingsPeriode.flatMap { it.ikkeOverlappendePerioder(ikkeOppfyltPerioder) }
        }
}

 */
