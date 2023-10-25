package no.nav.tiltakspenger.domene.saksopplysning

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import kotlin.test.Test

internal class SaksopplysningTest {

    @Test
    fun `sjekk at oppdatering av saksopplysninger fjerner saksbehandler`() {
        val sakbehandlerOpplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                saksbehandler = null,
            )

        val nySaksopplysning =
            Saksopplysning(
                fom = 15.januar(2023),
                tom = 15.mars(2023),
                kilde = Kilde.ARENA,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(SakId.random(), nySøknadMedBrukerTiltak()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.AAP }.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning)
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.AAP }.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.AAP }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE

        val behandlingOppdatertMedNyDataFraAAP = behandling.leggTilSaksopplysning(nySaksopplysning)
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 1
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.first { it.vilkår == Vilkår.AAP }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
    }

    @Test
    fun `hvis det finnes ytelse i starten av en vurderingsperiode får man IkkeOppfylt i denne perioden og Oppfylt i resten`() {
        val periode = Periode(fra = 1.januar(2023), til = 31.mars(2023))

        val saksopplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.januar(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        saksopplysning.lagVurdering(periode) shouldContainAll listOf(
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.AAP,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.januar(2023),
                tom = 31.januar(2023),
            ),
            Vurdering.Oppfylt(
                vilkår = Vilkår.AAP,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.februar(2023),
                tom = 31.mars(2023),
            ),
        )
    }
}