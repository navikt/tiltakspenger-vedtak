package no.nav.tiltakspenger.saksbehandling.domene.behandling

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingVilkårsvurdertInnvilget
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import org.junit.jupiter.api.Test

internal class BehandlingVilkårsvurdertTest {

    @Test
    fun `sjekk at delvis innvilget blir innvilget`() {
        val foreldrepenger = YtelseSaksopplysning(
            kilde = Kilde.K9SAK,
            vilkår = Vilkår.FORELDREPENGER,
            saksbehandler = null,
            periode = Periode(
                1.januar(2024),
                31.januar(2024),
            ),
            detaljer = "",
            harYtelse = true,
        )

        val behandlingInnvilget = behandlingVilkårsvurdertInnvilget(
            periode = Periode(1.januar(2024), 31.mars(2024)),
        )

        // Alt ok
        behandlingInnvilget.shouldBeInstanceOf<BehandlingVilkårsvurdert>()
        behandlingInnvilget.status shouldBe BehandlingStatus.Innvilget

        // Legg til Foreldrepenger i januar. Skal fortsatt være innvilget med januar git ikke rett
        val behandling = behandlingInnvilget.leggTilSaksopplysning(listOf(foreldrepenger))
        behandling.shouldBeInstanceOf<BehandlingVilkårsvurdert>()
        behandling.status shouldBe BehandlingStatus.Innvilget

        behandling.utfallsperioder shouldContainExactlyInAnyOrder listOf(
            Utfallsperiode(
                fom = 1.januar(2024),
                tom = 31.januar(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER,
            ),
            Utfallsperiode(
                fom = 1.februar(2024),
                tom = 31.mars(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
            ),
        )

        // Legg til pensjon i mars. Skal fortsatt være innvilget, men ingen rett i januar og mars
        val pensjon = YtelseSaksopplysning(
            kilde = Kilde.PESYS,
            vilkår = Vilkår.GJENLEVENDEPENSJON,
            saksbehandler = null,
            detaljer = "",
            periode = Periode(1.mars(2024), 31.mars(2024)),
            harYtelse = true,
        )

        val behandlingMedYtelseStartOgSlutt = behandling.leggTilSaksopplysning(listOf(pensjon))
        behandlingMedYtelseStartOgSlutt.shouldBeInstanceOf<BehandlingVilkårsvurdert>()
        behandlingMedYtelseStartOgSlutt.status shouldBe BehandlingStatus.Innvilget

        behandlingMedYtelseStartOgSlutt.utfallsperioder shouldContainExactlyInAnyOrder listOf(
            Utfallsperiode(
                fom = 1.januar(2024),
                tom = 31.januar(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER,
            ),
            Utfallsperiode(
                fom = 1.februar(2024),
                tom = 29.februar(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
            ),
            Utfallsperiode(
                fom = 1.mars(2024),
                tom = 31.mars(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER,
            ),
        )

        // Legg til kvp i februar. Ingen perioder med rett er igjen så da blir hele avslag
        val kvp = YtelseSaksopplysning(
            kilde = Kilde.SAKSB,
            vilkår = Vilkår.KVP,
            saksbehandler = "Z12345",
            periode = Periode(
                1.februar(2024),
                29.februar(2024),
            ),
            harYtelse = true,
            detaljer = "",
        )

        val behandlingAvslag = behandlingMedYtelseStartOgSlutt.leggTilSaksopplysning(listOf(kvp))
        behandlingAvslag.shouldBeInstanceOf<BehandlingVilkårsvurdert>()
        behandlingAvslag.status shouldBe BehandlingStatus.Avslag

        behandlingAvslag.utfallsperioder shouldContainExactlyInAnyOrder listOf(
            Utfallsperiode(
                fom = 1.januar(2024),
                tom = 31.mars(2024),
                antallBarn = 0,
                utfall = UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER,
            ),
        )
    }
}
