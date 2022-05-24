package no.nav.tiltakspenger.domene.hågen

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.fakta.Faktum2
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class KVPVilkårsvurderingTest {

    @Test
    fun `skal gi ønsket resultat`() {
        val toUker = Periode(
            fra = LocalDate.now().minusWeeks(1),
            til = LocalDate.now().plusWeeks(1)
        )

        //Lage vilkårsvurderingstreet
        val kvpVilkårsvurderingBruker = KVPVBrukerVilkårsvurdering()
        val kvpVilkårvurderingBruker = KVPRegisterVilkårsvurdering()
        val kvpVilkårsvurderingSaksbehandler = KVPSaksbehandlerVilkårsvurdering()
        val kvpVilkårsvurdering = KVPVilkårsvurdering(
            vilkårsvurderingBruker = kvpVilkårsvurderingBruker,
            vilkårsvurderingRegister = kvpVilkårvurderingBruker,
            vilkårsvurderingSaksbehandler = kvpVilkårsvurderingSaksbehandler,
        )

        //Fylle inn fakta i vilkårsvurderingstreet
        kvpVilkårsvurdering.fyllInnFaktumDerDetPasser(
            Faktum2(
                tilstand = Faktum2.Tilstand.KJENT,
                verdi = KVPFaktumBruker(deltarKVP = true)
            )
        )
        kvpVilkårsvurdering.fyllInnFaktumDerDetPasser(
            Faktum2(
                tilstand = Faktum2.Tilstand.KJENT,
                verdi = KVPFaktumSaksbehandler(deltarKVP = false)
            )
        )

        //Hente ut utfall
        assertEquals(Utfall.VurdertOgTrengerManuellBehandling, kvpVilkårsvurderingBruker.vurder(toUker).utfall)
        assertEquals(toUker, kvpVilkårsvurderingBruker.vurder(toUker).periode)
        assertEquals(
            Utfallsperiode(Utfall.VurdertOgOppfylt, toUker),
            kvpVilkårsvurdering.vurder(periode = toUker)
        )
    }
}