package no.nav.tiltakspenger.domene.behandling

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingVilkårsvurdertInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksopplysning
import org.junit.jupiter.api.Test

internal class BehandlingVilkårsvurdertTest {

    @Test
    fun `sjekk at delvis innvilget blir innvilget`() {
        val foreldrepenger = saksopplysning(
            fom = 1.januar(2024),
            tom = 31.januar(2024),
            kilde = Kilde.K9SAK,
            vilkår = Vilkår.FORELDREPENGER,
            type = TypeSaksopplysning.HAR_YTELSE,
            saksbehandler = null,
        )

        val behandlingInnvilget = behandlingVilkårsvurdertInnvilget(
            periode = Periode(1.januar(2024), 31.mars(2024)),
        )

        behandlingInnvilget.shouldBeInstanceOf<BehandlingVilkårsvurdert.Innvilget>()

        val behandling = behandlingInnvilget.leggTilSaksopplysning(foreldrepenger).behandling
        behandling.shouldBeInstanceOf<BehandlingVilkårsvurdert.Innvilget>()

        behandling.utfallsperioder shouldContainExactlyInAnyOrder listOf(
            BehandlingVilkårsvurdert.Utfallsperiode(
                fom = 1.januar(2024),
                tom = 31.januar(2024),
                antallBarn = 0,
                tiltak = listOf(),
                antDagerMedTiltak = 0,
                utfall = BehandlingVilkårsvurdert.UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER,
            ),
            BehandlingVilkårsvurdert.Utfallsperiode(
                fom = 1.februar(2024),
                tom = 31.mars(2024),
                antallBarn = 0,
                tiltak = listOf(),
                antDagerMedTiltak = 0,
                utfall = BehandlingVilkårsvurdert.UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
            ),
        )
    }
}
