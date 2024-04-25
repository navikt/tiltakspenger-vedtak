package no.nav.tiltakspenger.vedtak.routes.behandling

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.hentUtfallForVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.settBeslutter
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SammenstillingForBehandlingDTOTest {

    @Test
    fun `finnStatus skal gi riktig statustekst basert på behandlingen`() {
        val opprettetBehandling = mockk<BehandlingOpprettet>()
        val opprettetStatus = finnStatus(opprettetBehandling)
        assert(opprettetStatus === "Klar til behandling")

        val avslåttBehandling = mockk<BehandlingIverksatt>()
        every { avslåttBehandling.status } returns BehandlingStatus.Avslag
        val avslagStatus = finnStatus(avslåttBehandling)
        assert(avslagStatus === "Iverksatt Avslag")

        val innvilgetBehandling = mockk<BehandlingIverksatt>()
        every { innvilgetBehandling.status } returns BehandlingStatus.Innvilget
        val innvilgetStatus = finnStatus(innvilgetBehandling)
        assert(innvilgetStatus === "Iverksatt Innvilget")
    }

    @Test
    fun `finnStatus skal gi riktig statustekst når behandlingen er sendt til beslutter`() {
        val behandlingTilBeslutter = mockk<BehandlingTilBeslutter>()
        every { behandlingTilBeslutter.beslutter } returns null
        val klarTilBeslutningTekst = finnStatus(behandlingTilBeslutter)
        assert(klarTilBeslutningTekst === "Klar til beslutning")

        every { behandlingTilBeslutter.beslutter } returns "test beslutter"
        val underBeslutningTekst = finnStatus(behandlingTilBeslutter)
        assert(underBeslutningTekst === "Under beslutning")
    }

    @Test
    fun `finnStatus skal gi riktig statustekst når behandlingen er ferdig vilkårsvurdert`() {
        val behandlingVilkårsvurdert = mockk<BehandlingVilkårsvurdert>()
        every { behandlingVilkårsvurdert.saksbehandler } returns null
        val klarTilBeslutningTekst = finnStatus(behandlingVilkårsvurdert)
        assert(klarTilBeslutningTekst === "Klar til behandling")

        every { behandlingVilkårsvurdert.saksbehandler } returns "test saksbehandler"
        val underBeslutningTekst = finnStatus(behandlingVilkårsvurdert)
        assert(underBeslutningTekst === "Under behandling")
    }

    private fun mockKreverManuellVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering =
        Vurdering(
            vilkår = vilkår,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            kilde = mockk<Kilde>(),
            detaljer = "test",
            utfall = Utfall.KREVER_MANUELL_VURDERING,
        )

    private fun mockOppfyltVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering = Vurdering(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        kilde = mockk<Kilde>(),
        detaljer = "test",
        utfall = Utfall.OPPFYLT,
    )

    private fun mockIkkeOppfyltVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering = Vurdering(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        kilde = mockk<Kilde>(),
        detaljer = "test",
        utfall = Utfall.IKKE_OPPFYLT,
    )

    @Test
    fun `hentUtfallForVilkår skal gi KREVER_MANUELL_VURDERING hvis noen vurderinger på vilkåret krever manuell vurdering`() {
        val aapVurderingManuell = mockKreverManuellVurdering()
        val aapVurderingOppfylt = mockOppfyltVurdering()
        val vurderinger = listOf(aapVurderingManuell, aapVurderingOppfylt)
        val utfall = hentUtfallForVilkår(Vilkår.AAP, vurderinger)
        assert(utfall == Utfall.KREVER_MANUELL_VURDERING)
    }

    @Test
    fun `hentUtfallForVilkår skal gi IKKE_OPPFYLT hvis noen vurderinger på vilkåret ikke er oppfylt`() {
        val aapVurderingOppfylt = mockOppfyltVurdering()
        val aapVurderingIkkeOppfylt = mockIkkeOppfyltVurdering()
        val vurderinger = listOf(aapVurderingOppfylt, aapVurderingIkkeOppfylt)
        val utfall = hentUtfallForVilkår(Vilkår.AAP, vurderinger)
        assert(utfall == Utfall.IKKE_OPPFYLT)
    }

    @Test
    fun `hentUtfallForVilkår skal gi OPPFYLT hvis alle vurderinger på vilkåret oppfylt`() {
        val aapVurderingOppfylt = mockOppfyltVurdering()
        val vurderinger = listOf(aapVurderingOppfylt)
        val utfall = hentUtfallForVilkår(Vilkår.AAP, vurderinger)
        assert(utfall == Utfall.OPPFYLT)
    }

    private fun mockSaksopplysning(vilkår: Vilkår = Vilkår.AAP): Saksopplysning = Saksopplysning(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        detaljer = "test",
        kilde = mockk<Kilde>(),
        typeSaksopplysning = mockk<TypeSaksopplysning>(),
        saksbehandler = "test",
    )
    // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
//    @Test
//    fun `settUtfall svarer med utfall sålenge behandlingen er enten vilkårsvurdert, til beslutter, eller iverksatt`() {
//        val saksopplysning = mockSaksopplysning()
//
//        val iverksatt = mockk<BehandlingIverksatt>()
//        every { iverksatt.vilkårsvurderinger } returns emptyList()
//        val iverksattUtfall = settUtfall(iverksatt, saksopplysning)
//        assert(iverksattUtfall == Utfall.OPPFYLT.name)
//
//        val vilkårsvurdert = mockk<BehandlingVilkårsvurdert>()
//        every { vilkårsvurdert.vilkårsvurderinger } returns emptyList()
//        val vilkårsvurdertUtfall = settUtfall(vilkårsvurdert, saksopplysning)
//        assert(vilkårsvurdertUtfall == Utfall.OPPFYLT.name)
//
//        val tilBeslutter = mockk<BehandlingVilkårsvurdert>()
//        every { tilBeslutter.vilkårsvurderinger } returns emptyList()
//        val tilBeslutterUtfall = settUtfall(vilkårsvurdert, saksopplysning)
//        assert(tilBeslutterUtfall == Utfall.OPPFYLT.name)
//    }
//
//    @Test
//    fun `settSamletUtfall svarer med IKKE_OPPFYLT hvis noen av utfallene ikke er oppfylt`() {
//        val behandling = mockk<BehandlingIverksatt>()
//        val saksopplysninger = listOf(mockSaksopplysning())
//        val ikkeOppfyltVurdering = mockIkkeOppfyltVurdering()
//        val vilkårsvurderinger = listOf(ikkeOppfyltVurdering)
//        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
//
//        val samletUtfall = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
//        assert(samletUtfall == Utfall.IKKE_OPPFYLT.name)
//    }
//
//    @Test
//    fun `settSamletUtfall svarer med KREVER_MANUELL_VURDERING hvis noen av utfallene er Krever Manuell Vurdering`() {
//        val behandling = mockk<BehandlingIverksatt>()
//        val saksopplysninger = listOf(mockSaksopplysning())
//        val manuellVurdering = mockKreverManuellVurdering()
//        val vilkårsvurderinger = listOf(manuellVurdering)
//        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
//
//        val samletUtfall = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
//        assert(samletUtfall == Utfall.KREVER_MANUELL_VURDERING.name)
//    }
//
//    @Test
//    fun `settSamletUtfall svarer kun med OPPFYLT hvis alle vurderingene er oppfylt`() {
//        val behandling = mockk<BehandlingIverksatt>()
//        val saksopplysninger = listOf(mockSaksopplysning())
//        val oppfyltVurdering = mockOppfyltVurdering()
//        val vilkårsvurderinger = listOf(oppfyltVurdering)
//        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
//
//        val samletUtfallOppfylt = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
//        assert(samletUtfallOppfylt == Utfall.OPPFYLT.name)
//
//        val ikkeOppfyltVurdering = mockIkkeOppfyltVurdering()
//        every { behandling.vilkårsvurderinger } returns listOf(oppfyltVurdering, ikkeOppfyltVurdering, oppfyltVurdering)
//        val samletUtfallIkkeOppfylt = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
//        assert(samletUtfallIkkeOppfylt == Utfall.IKKE_OPPFYLT.name)
//
//        val manuellVurdering = mockKreverManuellVurdering()
//        every { behandling.vilkårsvurderinger } returns listOf(oppfyltVurdering, manuellVurdering, oppfyltVurdering)
//        val samletUtfallManuellVurdering = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
//        assert(samletUtfallManuellVurdering == Utfall.KREVER_MANUELL_VURDERING.name)
//    }

    @Test
    fun `settBeslutter skal kun svare med beslutter hvis behandlingen er iverksatt, eller til beslutter`() {
        val beslutter = "Test Beslutter"

        val behandlingIverksatt = mockk<BehandlingIverksatt>()
        every { behandlingIverksatt.beslutter } returns beslutter
        val iverksattBeslutter = settBeslutter(behandlingIverksatt)
        assert(iverksattBeslutter == beslutter)

        val behandlingTilBeslutter = mockk<BehandlingTilBeslutter>()
        every { behandlingTilBeslutter.beslutter } returns beslutter
        val tilBeslutter = settBeslutter(behandlingTilBeslutter)
        assert(tilBeslutter == beslutter)

        val behandlingVilkårsvurdert = mockk<BehandlingVilkårsvurdert>()
        val vilkårsvurdertBeslutter = settBeslutter(behandlingVilkårsvurdert)
        assert(vilkårsvurdertBeslutter == null)
    }
}
