package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class RevurderingTilBeslutter(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val livsoppholdVilkårData: LivsoppholdVilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val forrigeVedtak: Vedtak,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>?,
    override val søknader: List<Søknad>,
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String?,
    val status: BehandlingStatus,
) : Revurderingsbehandling {

    fun iverksett(utøvendeBeslutter: Saksbehandler): RevurderingIverksatt {
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        check(utøvendeBeslutter.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }
        check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("En behandling til beslutter kan ikke være manuell")
            BehandlingStatus.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 ${this.id}")
            else -> RevurderingIverksatt(
                id = id,
                sakId = sakId,
                forrigeVedtak = forrigeVedtak,
                vurderingsperiode = vurderingsperiode,
                livsoppholdVilkårData = livsoppholdVilkårData,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
                status = status,
                søknader = forrigeVedtak.behandling.søknader,
            )
        }
    }

    fun sendTilbake(utøvendeBeslutter: Saksbehandler): RevurderingVilkårsvurdert {
        check(utøvendeBeslutter.isBeslutter() || utøvendeBeslutter.isAdmin()) { "Saksbehandler må være beslutter eller administrator" }
        check(this.beslutter == utøvendeBeslutter.navIdent || utøvendeBeslutter.isAdmin()) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
        return RevurderingVilkårsvurdert(
            id = id,
            sakId = sakId,
            forrigeVedtak = forrigeVedtak,
            vurderingsperiode = vurderingsperiode,
            livsoppholdVilkårData = livsoppholdVilkårData,
            tiltak = tiltak,
            vilkårsvurderinger = vilkårsvurderinger,
            utfallsperioder = utfallsperioder,
            saksbehandler = saksbehandler,
            status = BehandlingStatus.Innvilget,
            søknader = søknader,
        )
    }

    override fun leggTilSaksopplysning(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): LeggTilSaksopplysningRespons =
        spolTilbake().leggTilSaksopplysning(livsoppholdSaksopplysning)

    override fun startBehandling(saksbehandler: Saksbehandler): Revurderingsbehandling {
        check(this.beslutter == null) { "Denne behandlingen har allerede en beslutter" }
        check(saksbehandler.isBeslutter()) { "Saksbehandler må være beslutter" }
        return this.copy(
            beslutter = saksbehandler.navIdent,
        )
    }

    private fun spolTilbake(): RevurderingOpprettet =
        RevurderingOpprettet(
            id = id,
            sakId = sakId,
            forrigeVedtak = forrigeVedtak,
            vurderingsperiode = vurderingsperiode,
            livsoppholdVilkårData = livsoppholdVilkårData,
            tiltak = tiltak,
            saksbehandler = saksbehandler,
            søknader = søknader,
        )
}
