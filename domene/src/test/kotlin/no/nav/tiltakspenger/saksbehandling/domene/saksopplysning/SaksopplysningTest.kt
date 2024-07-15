package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import org.junit.jupiter.api.Disabled
import kotlin.test.Test

internal class SaksopplysningTest {

    @Test
    fun `sjekk at oppdatering av saksopplysninger fjerner saksbehandler`() {
        val sakbehandlerOpplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                saksbehandler = null,
            )

        val nySaksopplysning =
            Saksopplysning(
                fom = 15.januar(2023),
                tom = 15.mars(2023),
                kilde = Kilde.ARENA,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling =
            Førstegangsbehandling.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.FORELDREPENGER }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE

        val behandlingOppdatertMedNyDataFraAAP = behandling.leggTilSaksopplysning(nySaksopplysning).behandling
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 1
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
    }

    @Test
    fun `ny søknad med samme saksopplysning fjerner ikke saksbehandler`() {
        val sakbehandlerOpplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.INTROPROGRAMMET,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling = Førstegangsbehandling.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE

        val behandlingMedUendretSøknad = behandlingMedSaksbehandler.leggTilSøknad(nySøknad())
        behandlingMedUendretSøknad.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedUendretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE
        behandlingMedUendretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
    }

    @Disabled("kew: Disabler testen siden vi ikke skal tenke på 2 søknader per nå.")
    @Test
    fun `ny søknad med en annen saksopplysning fjerner saksbehandler`() {
        val sakbehandlerOpplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.INTROPROGRAMMET,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling = Førstegangsbehandling.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_IKKE_YTELSE
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE

        val behandlingMedEndretSøknad = behandlingMedSaksbehandler.leggTilSøknad(nySøknad(intro = periodeJa()))
        behandlingMedEndretSøknad.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandlingMedEndretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
    }

    @Test
    fun `hvis det finnes ytelse i starten av en vurderingsperiode får man IkkeOppfylt i denne perioden og Oppfylt i resten`() {
        val periode = Periode(fraOgMed = 1.januar(2023), tilOgMed = 31.mars(2023))

        val saksopplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.januar(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        saksopplysning.lagVurdering(periode) shouldContainAll listOf(
            Vurdering(
                vilkår = Vilkår.FORELDREPENGER,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.januar(2023),
                tom = 31.januar(2023),
                utfall = Utfall.IKKE_OPPFYLT,
                grunnlagId = null,
            ),
            Vurdering(
                vilkår = Vilkår.FORELDREPENGER,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.februar(2023),
                tom = 31.mars(2023),
                utfall = Utfall.OPPFYLT,
                grunnlagId = null,
            ),
        )
    }
}
