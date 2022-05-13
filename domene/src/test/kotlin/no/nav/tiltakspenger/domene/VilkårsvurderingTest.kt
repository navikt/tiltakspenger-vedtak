package no.nav.tiltakspenger.domene

import KVP
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
        assertTrue(vilkårsvurdering.utfall is Utfall.IkkeVurdert)
    }

    @Test
    fun `en vilkårsvurdering som mottar riktig fakta har utfall VURDERT_OG_OPPFYLT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        assertTrue(vilkårsvurdering.utfall is Utfall.IkkeVurdert)

        val vurderingMedUtfall =
            vilkårsvurdering.vurder(FødselsdatoFaktum(fødselsdato = 12.april(2019), kilde = FaktumKilde.BRUKER))
        assertTrue(vurderingMedUtfall.utfall is Utfall.VurdertOgOppfylt)
    }

    @Test
    fun `en vilkårsvurdering om KVP hvor bruker sier hen går på det skal til manuell behandling`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = KVP,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        val vurderingMedUtfall = vilkårsvurdering.vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
        assertTrue(vurderingMedUtfall.utfall is Utfall.VurdertOgTrengerManuellBehandling)
    }

    @Test
    fun `en KVP vilkårsvurdering med fakta fra bruker og fakta fra saksbehandler skal avgjøres ved holmgang`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = KVP,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
            .vurder(KVPFaktum(deltarKVP = false, kilde = FaktumKilde.SAKSBEHANDLER))
        assertTrue(vurderingMedUtfall.utfall is Utfall.VurdertOgOppfylt)
    }

    @Test
    fun `en bruker bor på institusjon i deler av vurderingsperioden`() {
        val start = LocalDate.now()
        val om1Uke = start.plusWeeks(1)
        val om2Uker = start.plusWeeks(2)

        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = start, til = om2Uker)
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(
                InstitusjonsoppholdsFaktum(
                    opphold = false,
                    kilde = FaktumKilde.BRUKER,
                    oppholdsperiode = Periode(fra = start, til = om1Uke),
                    friKostOgLosji = false
                )
            )

        assertTrue(vurderingMedUtfall.utfall is Utfall.VurdertOgOppfylt)
        when (val utfall = vurderingMedUtfall.utfall) {
            is Utfall.VurdertOgOppfylt -> {
                assertTrue(utfall.vilkårOppfyltPeriode.fra.isEqual(om1Uke))
                assertTrue(utfall.vilkårOppfyltPeriode.til.isEqual(om2Uker))
            }
        }


    }
}
