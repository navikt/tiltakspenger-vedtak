package no.nav.tiltakspenger.vedtak.routes.behandling

/*
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.getBeslutter
import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.settAntallDagerSaksopplysninger
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
 */

// TODO: Må fikses
class SammenstillingForBehandlingDTOTest {

    /*
    @Test
    fun `finnStatus skal gi riktig statustekst basert på behandlingen`() {
        val opprettetBehandling = mockk<Førstegangsbehandling>()
        every { opprettetBehandling.tilstand } returns BehandlingTilstand.OPPRETTET
        val opprettetStatus = finnStatus(opprettetBehandling)
        assert(opprettetStatus === "Klar til behandling")

        val avslåttBehandling = mockk<Førstegangsbehandling>()
        every { avslåttBehandling.tilstand } returns BehandlingTilstand.IVERKSATT
        every { avslåttBehandling.status } returns BehandlingStatus.Avslag
        val avslagStatus = finnStatus(avslåttBehandling)
        assert(avslagStatus === "Iverksatt Avslag")

        val innvilgetBehandling = mockk<Førstegangsbehandling>()
        every { innvilgetBehandling.tilstand } returns BehandlingTilstand.IVERKSATT
        every { innvilgetBehandling.status } returns BehandlingStatus.Innvilget
        val innvilgetStatus = finnStatus(innvilgetBehandling)
        assert(innvilgetStatus === "Iverksatt Innvilget")
    }

    @Test
    fun `finnStatus skal gi riktig statustekst når behandlingen er sendt til beslutter`() {
        val behandlingTilBeslutter = mockk<Førstegangsbehandling>()
        every { behandlingTilBeslutter.tilstand } returns BehandlingTilstand.TIL_BESLUTTER
        every { behandlingTilBeslutter.beslutter } returns null
        val klarTilBeslutningTekst = finnStatus(behandlingTilBeslutter)
        assert(klarTilBeslutningTekst === "Klar til beslutning")

        every { behandlingTilBeslutter.beslutter } returns "test beslutter"
        val underBeslutningTekst = finnStatus(behandlingTilBeslutter)
        assert(underBeslutningTekst === "Under beslutning")
    }

    @Test
    fun `finnStatus skal gi riktig statustekst når behandlingen er ferdig vilkårsvurdert`() {
        val behandlingVilkårsvurdert = mockk<Førstegangsbehandling>()
        every { behandlingVilkårsvurdert.tilstand } returns BehandlingTilstand.VILKÅRSVURDERT
        every { behandlingVilkårsvurdert.saksbehandler } returns null
        val klarTilBeslutningTekst = finnStatus(behandlingVilkårsvurdert)
        assert(klarTilBeslutningTekst === "Klar til behandling")

        every { behandlingVilkårsvurdert.saksbehandler } returns "test saksbehandler"
        val underBeslutningTekst = finnStatus(behandlingVilkårsvurdert)
        assert(underBeslutningTekst === "Under behandling")
    }

    private fun mockKreverManuellVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering.KreverManuellVurdering =
        Vurdering.KreverManuellVurdering(
            vilkår = vilkår,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            kilde = mockk<Kilde>(),
            detaljer = "test",
        )

    private fun mockOppfyltVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering.Oppfylt = Vurdering.Oppfylt(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        kilde = mockk<Kilde>(),
        detaljer = "test",
    )

    private fun mockIkkeOppfyltVurdering(vilkår: Vilkår = Vilkår.AAP): Vurdering.IkkeOppfylt = Vurdering.IkkeOppfylt(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        kilde = mockk<Kilde>(),
        detaljer = "test",
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

    private fun mockSaksopplysning(vilkår: Vilkår = Vilkår.AAP): LivsoppholdSaksopplysning = LivsoppholdSaksopplysning(
        vilkår = vilkår,
        fom = LocalDate.now(),
        tom = LocalDate.now(),
        detaljer = "test",
        kilde = mockk<Kilde>(),
        harYtelse = mockk<HarYtelse>(),
        saksbehandler = "test",
    )

    @Test
    fun `settUtfall svarer med utfall sålenge behandlingen er enten vilkårsvurdert, til beslutter, eller iverksatt`() {
        val saksopplysning = mockSaksopplysning()

        val iverksatt = mockk<Førstegangsbehandling>()
        every { iverksatt.tilstand } returns BehandlingTilstand.IVERKSATT
        every { iverksatt.vilkårsvurderinger } returns emptyList()
        val iverksattUtfall = settUtfall(iverksatt, saksopplysning)
        assert(iverksattUtfall == Utfall.OPPFYLT.name)

        val vilkårsvurdert = mockk<Førstegangsbehandling>()
        every { vilkårsvurdert.tilstand } returns BehandlingTilstand.VILKÅRSVURDERT
        every { vilkårsvurdert.vilkårsvurderinger } returns emptyList()
        val vilkårsvurdertUtfall = settUtfall(vilkårsvurdert, saksopplysning)
        assert(vilkårsvurdertUtfall == Utfall.OPPFYLT.name)

        val tilBeslutter = mockk<Førstegangsbehandling>()
        every { tilBeslutter.tilstand } returns BehandlingTilstand.TIL_BESLUTTER
        every { tilBeslutter.vilkårsvurderinger } returns emptyList()
        val tilBeslutterUtfall = settUtfall(vilkårsvurdert, saksopplysning)
        assert(tilBeslutterUtfall == Utfall.OPPFYLT.name)
    }

    @Test
    fun `settSamletUtfall svarer med IKKE_OPPFYLT hvis noen av utfallene ikke er oppfylt`() {
        val behandling = mockk<Førstegangsbehandling>()
        val saksopplysninger = listOf(mockSaksopplysning())
        val ikkeOppfyltVurdering = mockIkkeOppfyltVurdering()
        val vilkårsvurderinger = listOf(ikkeOppfyltVurdering)
        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
        every { behandling.tilstand } returns BehandlingTilstand.IVERKSATT

        val samletUtfall = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
        assert(samletUtfall == Utfall.IKKE_OPPFYLT.name)
    }

    @Test
    fun `settSamletUtfall svarer med KREVER_MANUELL_VURDERING hvis noen av utfallene er Krever Manuell Vurdering`() {
        val behandling = mockk<Førstegangsbehandling>()
        val saksopplysninger = listOf(mockSaksopplysning())
        val manuellVurdering = mockKreverManuellVurdering()
        val vilkårsvurderinger = listOf(manuellVurdering)
        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
        every { behandling.tilstand } returns BehandlingTilstand.IVERKSATT

        val samletUtfall = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
        assert(samletUtfall == Utfall.KREVER_MANUELL_VURDERING.name)
    }

    @Test
    fun `settSamletUtfall svarer kun med OPPFYLT hvis alle vurderingene er oppfylt`() {
        val behandling = mockk<Førstegangsbehandling>()
        val saksopplysninger = listOf(mockSaksopplysning())
        val oppfyltVurdering = mockOppfyltVurdering()
        val vilkårsvurderinger = listOf(oppfyltVurdering)
        every { behandling.vilkårsvurderinger } returns vilkårsvurderinger
        every { behandling.tilstand } returns BehandlingTilstand.IVERKSATT

        val samletUtfallOppfylt = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
        assert(samletUtfallOppfylt == Utfall.OPPFYLT.name)

        val ikkeOppfyltVurdering = mockIkkeOppfyltVurdering()
        every { behandling.vilkårsvurderinger } returns listOf(oppfyltVurdering, ikkeOppfyltVurdering, oppfyltVurdering)
        val samletUtfallIkkeOppfylt = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
        assert(samletUtfallIkkeOppfylt == Utfall.IKKE_OPPFYLT.name)

        val manuellVurdering = mockKreverManuellVurdering()
        every { behandling.vilkårsvurderinger } returns listOf(oppfyltVurdering, manuellVurdering, oppfyltVurdering)
        val samletUtfallManuellVurdering = settSamletUtfallForSaksopplysninger(behandling, saksopplysninger)
        assert(samletUtfallManuellVurdering == Utfall.KREVER_MANUELL_VURDERING.name)
    }

    @Test
    fun `settBeslutter skal kun svare med beslutter hvis behandlingen er iverksatt, eller til beslutter`() {
        val beslutter = "Test Beslutter"

        val behandlingIverksatt = mockk<Førstegangsbehandling>()
        every { behandlingIverksatt.tilstand } returns BehandlingTilstand.IVERKSATT
        every { behandlingIverksatt.beslutter } returns beslutter
        val iverksattBeslutter = getBeslutter(behandlingIverksatt)
        assert(iverksattBeslutter == beslutter)

        val behandlingTilBeslutter = mockk<Førstegangsbehandling>()
        every { behandlingTilBeslutter.tilstand } returns BehandlingTilstand.TIL_BESLUTTER
        every { behandlingTilBeslutter.beslutter } returns beslutter
        val tilBeslutter = getBeslutter(behandlingTilBeslutter)
        assert(tilBeslutter == beslutter)

<<<<<<< HEAD
        val behandlingVilkårsvurdert = mockk<BehandlingVilkårsvurdert>()
        val vilkårsvurdertBeslutter = getBeslutter(behandlingVilkårsvurdert)
=======
        val behandlingVilkårsvurdert = mockk<Førstegangsbehandling>()
        every { behandlingVilkårsvurdert.tilstand } returns BehandlingTilstand.VILKÅRSVURDERT
        val vilkårsvurdertBeslutter = settBeslutter(behandlingVilkårsvurdert)
>>>>>>> main
        assert(vilkårsvurdertBeslutter == null)
    }

    @Test
    fun `settAntallDager skal mappe AntallDagerSaksopplysning-data til et format som gir mer mening for frontend`() {
        val tiltak = mockk<Tiltak>()

        val antallDagerMock = AntallDagerDTO(
            antallDager = 2,
            periode = PeriodeDTO(
                fra = LocalDate.MIN,
                til = LocalDate.MAX,
            ),
            kilde = Kilde.ARENA.toString(),
        )

        every { tiltak.antallDagerSaksopplysninger } returns AntallDagerSaksopplysninger(
            antallDagerSaksopplysningerFraSBH = emptyList(),
            avklartAntallDager = emptyList(),
            antallDagerSaksopplysningerFraRegister = listOf(
                PeriodeMedVerdi(
                    verdi = AntallDager(
                        kilde = Kilde.valueOf(antallDagerMock.kilde.uppercase()),
                        antallDager = antallDagerMock.antallDager,
                        saksbehandlerIdent = null,
                    ),
                    periode = Periode(
                        fra = antallDagerMock.periode.fra,
                        til = antallDagerMock.periode.til,
                    ),
                ),
            ),
        )
        val resultat = settAntallDagerSaksopplysninger(
            antallDagerSaksopplysninger = tiltak.antallDagerSaksopplysninger,
        )

        val saksopplysningElement = resultat.antallDagerSaksopplysningerFraRegister.get(0)
        assertNotNull(saksopplysningElement)
        assertEquals(saksopplysningElement.antallDager, antallDagerMock.antallDager)
        assertEquals(saksopplysningElement.periode.fra, antallDagerMock.periode.fra)
        assertEquals(saksopplysningElement.periode.til, antallDagerMock.periode.til)
        assertEquals(saksopplysningElement.kilde, antallDagerMock.kilde)
    }

     */
}
