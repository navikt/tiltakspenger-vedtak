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
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder
import kotlin.test.Test

internal class LivsoppholdSaksopplysningTest {

    @Test
    fun `sjekk at oppdatering av saksopplysninger fjerner saksbehandler`() {
        val sakbehandlerOpplysning =
            LivsoppholdSaksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                harYtelse = HarYtelse.HAR_IKKE_YTELSE,
                saksbehandler = null,
            )

        val nyLivsoppholdSaksopplysning =
            LivsoppholdSaksopplysning(
                fom = 15.januar(2023),
                tom = 15.mars(2023),
                kilde = Kilde.ARENA,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                harYtelse = HarYtelse.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling =
            BehandlingOpprettet.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.harYtelse shouldBe HarYtelse.IKKE_INNHENTET_ENDA

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.harYtelse shouldBe HarYtelse.IKKE_INNHENTET_ENDA
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.FORELDREPENGER }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE

        val behandlingOppdatertMedNyDataFraAAP =
            behandling.leggTilSaksopplysning(nyLivsoppholdSaksopplysning).behandling
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.filter { it.vilkår == Vilkår.FORELDREPENGER }.size shouldBe 1
        behandlingOppdatertMedNyDataFraAAP.saksopplysninger.first { it.vilkår == Vilkår.FORELDREPENGER }.harYtelse shouldBe HarYtelse.HAR_YTELSE
    }

    @Test
    fun `ny søknad med samme saksopplysning fjerner ikke saksbehandler`() {
        val sakbehandlerOpplysning =
            LivsoppholdSaksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.INTROPROGRAMMET,
                detaljer = "",
                harYtelse = HarYtelse.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling = BehandlingOpprettet.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.harYtelse shouldBe HarYtelse.HAR_YTELSE

        val behandlingMedUendretSøknad = behandlingMedSaksbehandler.leggTilSøknad(nySøknad())
        behandlingMedUendretSøknad.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedUendretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE
        behandlingMedUendretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.harYtelse shouldBe HarYtelse.HAR_YTELSE
    }

    @Test
    fun `ny søknad med en annen saksopplysning fjerner saksbehandler`() {
        val sakbehandlerOpplysning =
            LivsoppholdSaksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.INTROPROGRAMMET,
                detaljer = "",
                harYtelse = HarYtelse.HAR_YTELSE,
                saksbehandler = null,
            )

        val behandling = BehandlingOpprettet.opprettBehandling(SakId.random(), nySøknad()).vilkårsvurder()
        behandling.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandling.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE

        val behandlingMedSaksbehandler = behandling.leggTilSaksopplysning(sakbehandlerOpplysning).behandling
        behandlingMedSaksbehandler.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 2
        behandlingMedSaksbehandler.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SØKNAD }.harYtelse shouldBe HarYtelse.HAR_IKKE_YTELSE
        behandlingMedSaksbehandler.saksopplysninger.last { it.vilkår == Vilkår.INTROPROGRAMMET && it.kilde == Kilde.SAKSB }.harYtelse shouldBe HarYtelse.HAR_YTELSE

        val behandlingMedEndretSøknad = behandlingMedSaksbehandler.leggTilSøknad(nySøknad(intro = periodeJa()))
        behandlingMedEndretSøknad.saksopplysninger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }.size shouldBe 1
        behandlingMedEndretSøknad.saksopplysninger.first { it.vilkår == Vilkår.INTROPROGRAMMET }.harYtelse shouldBe HarYtelse.HAR_YTELSE
    }

    @Test
    fun `hvis det finnes ytelse i starten av en vurderingsperiode får man IkkeOppfylt i denne perioden og Oppfylt i resten`() {
        val periode = Periode(fra = 1.januar(2023), til = 31.mars(2023))

        val livsoppholdSaksopplysning =
            LivsoppholdSaksopplysning(
                fom = 1.januar(2023),
                tom = 31.januar(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.FORELDREPENGER,
                detaljer = "",
                harYtelse = HarYtelse.HAR_YTELSE,
                saksbehandler = null,
            )

        livsoppholdSaksopplysning.lagVurdering(periode) shouldContainAll listOf(
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.FORELDREPENGER,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.januar(2023),
                tom = 31.januar(2023),
            ),
            Vurdering.Oppfylt(
                vilkår = Vilkår.FORELDREPENGER,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.februar(2023),
                tom = 31.mars(2023),
            ),
        )
    }
}
