package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class BehandlingTilBeslutter(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String?,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    fun iverksett(utøvendeBeslutter: Saksbehandler): BehandlingIverksatt {
        // checkNotNull(saksbehandler) { "Kan ikke iverksette en behandling uten saksbehandler" }
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        check(utøvendeBeslutter.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }
        check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("En behandling til beslutter kan ikke være manuell")
            BehandlingStatus.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 ${this.id}")
            else -> BehandlingIverksatt(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
                status = status,
            )
        }
    }

    fun sendTilbake(utøvendeBeslutter: Saksbehandler): BehandlingVilkårsvurdert {
        check(utøvendeBeslutter.isBeslutter() || utøvendeBeslutter.isAdmin()) { "Saksbehandler må være beslutter eller administrator" }
        check(this.beslutter == utøvendeBeslutter.navIdent || utøvendeBeslutter.isAdmin()) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
        return BehandlingVilkårsvurdert(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            vilkårsvurderinger = vilkårsvurderinger,
            utfallsperioder = utfallsperioder,
            saksbehandler = saksbehandler,
            status = BehandlingStatus.Innvilget,
        )
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert =
        this.spolTilbake().leggTilSøknad(søknad = søknad)

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons =
        this.spolTilbake().leggTilSaksopplysning(saksopplysning)

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.beslutter == null) { "Denne behandlingen har allerede en beslutter" }
        check(saksbehandler.isBeslutter()) { "Saksbehandler må være beslutter" }
        return this.copy(
            beslutter = saksbehandler.navIdent,
        )
    }

    private fun spolTilbake(): BehandlingOpprettet = BehandlingOpprettet(
        id = this.id,
        sakId = this.sakId,
        søknader = this.søknader,
        vurderingsperiode = this.vurderingsperiode,
        saksopplysninger = this.saksopplysninger,
        tiltak = this.tiltak,
        saksbehandler = this.saksbehandler,
        utfallsperioder = emptyList(), // TODO: Denne må endres til riktig verdi!! Gjør ikke dette helt enda siden det er litt lengre oppi veien
        vilkårData = TODO(), // TODO: Denne må endres til riktig verdi!! Gjør ikke dette helt enda siden det er litt lengre oppi veien
    )
}
