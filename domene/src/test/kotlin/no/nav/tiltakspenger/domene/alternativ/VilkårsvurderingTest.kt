package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class VilkårsvurderingTest {

    @Test
    fun `faktum som ikke passer blir ignorert`() {
        val periode = Periode(LocalDate.now().minusDays(2), LocalDate.now())
        val vilkårsvurdering = Over18Vilkårsvurdering()
        vilkårsvurdering.fyllInnFaktumDerDetPasser(Søknad(true))
        assertEquals(Utfall.IkkeVurdert, vilkårsvurdering.vurder(periode).utfallsperioder.first().utfall)
    }

    @Test
    fun `faktum som passer blir lagret`() {
        val periode = Periode(LocalDate.now().minusDays(2), LocalDate.now())
        val vilkårsvurdering = Over18Vilkårsvurdering()
        vilkårsvurdering.fyllInnFaktumDerDetPasser(FødselsdatoFaktum(LocalDate.now().minusYears(20)))
        assertEquals(Utfall.VurdertOgOppfylt, vilkårsvurdering.vurder(periode).utfallsperioder.first().utfall)
    }

    @Test
    fun `akkumulert vilkår prioriterer saksbehandlers faktum`() {
        val periode = Periode(LocalDate.now().minusDays(2), LocalDate.now())
        val vilkårsvurdering =
            KVPVilkårsvurdering(BrukerOppgittKVPVilkårsvurdering(), SaksbehandlerOppgittKVPVilkårsvurdering())

        vilkårsvurdering.fyllInnFaktumDerDetPasser(FødselsdatoFaktum(LocalDate.now().minusYears(20)))
        vilkårsvurdering.fyllInnFaktumDerDetPasser(Søknad(false))
        vilkårsvurdering.fyllInnFaktumDerDetPasser(SaksbehandlerOppgittKVPFaktum(true))

        assertEquals(KVPVilkår, vilkårsvurdering.vurder(periode).vilkår)
        assertEquals(1, vilkårsvurdering.vurder(periode).utfallsperioder.size)
        assertEquals(
            Utfall.VurdertOgIkkeOppfylt,
            vilkårsvurdering.vurder(periode).utfallsperioder.first().utfall
        )
        assertEquals(
            periode,
            vilkårsvurdering.vurder(periode).utfallsperioder.first().periode
        )
    }
}