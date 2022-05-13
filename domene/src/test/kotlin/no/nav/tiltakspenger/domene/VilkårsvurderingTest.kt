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
        assertEquals(Utfall.IKKE_VURDERT, vilkårsvurdering.utfall)
    }

    @Test
    fun `en vilkårsvurdering som mottar riktig fakta har utfall VURDERT_OG_OPPFYLT`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = ErOver18År,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        assertEquals(Utfall.IKKE_VURDERT, vilkårsvurdering.utfall)

        val vurderingMedUtfall =
            vilkårsvurdering.vurder(FødselsdatoFaktum(fødselsdato = 12.april(2019), kilde = FaktumKilde.BRUKER))
        assertEquals(Utfall.VURDERT_OG_OPPFYLT, vurderingMedUtfall.utfall)
    }

    @Test
    fun `en vilkårsvurdering om KVP hvor bruker sier hen går på det skal til manuell behandling`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = KVP,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        val vurderingMedUtfall = vilkårsvurdering.vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
        assertEquals(Utfall.VURDERT_OG_TRENGER_MANUELL_VURDERING, vurderingMedUtfall.utfall)
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
        assertEquals(Utfall.VURDERT_OG_OPPFYLT, vurderingMedUtfall.utfall)
    }

    @Test
    fun `en bruker bor på institusjon i deler av vurderingsperioden`() {
        val vilkårsvurdering = Vilkårsvurdering(
            vilkår = Institusjonsopphold,
            vurderingsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now())
        )
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(
                InstitusjonsoppholdsFaktum(
                    opphold = false,
                    kilde = FaktumKilde.BRUKER,
                    oppholdsperiode = Periode(fra = LocalDate.now(), til = LocalDate.now()),
                    friKostOgLosji = false
                )
            )

        assertEquals(Utfall.VURDERT_OG_IKKE_OPPFYLT, vurderingMedUtfall.utfall)

    }
}
