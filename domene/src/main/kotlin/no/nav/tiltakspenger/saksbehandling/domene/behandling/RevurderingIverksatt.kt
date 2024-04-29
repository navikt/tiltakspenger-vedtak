package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.SaksopplysningInterface
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.VilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class RevurderingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val vilkårData: VilkårData,
    override val tiltak: List<Tiltak>,
    override val forrigeVedtak: Vedtak,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode>,
    override val søknader: List<Søknad>,
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String,
    val status: BehandlingStatus,
) : Revurderingsbehandling {

    override fun leggTilSaksopplysning(saksopplysning: List<SaksopplysningInterface>): LeggTilSaksopplysningRespons {
        // TODO: Implementer denne metoden så den blir riktig (finnes oppgave på det i trello)
//        val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        vilkårData.leggTilSaksopplysning(saksopplysning)

//        return if () {
//            LeggTilSaksopplysningRespons(
//                behandling = this,
//                erEndret = false,
//            )
//        } else {
//            val nyBehandling = RevurderingOpprettet(
//                id = BehandlingId.random(),
//                sakId = this.sakId,
//                søknader = listOf(this.søknad()),
//                vurderingsperiode = this.vurderingsperiode,
//                vilkårData = vilkårData,
//                tiltak = this.tiltak,
//                saksbehandler = null,
//                forrigeVedtak = this.forrigeVedtak,
//            ).vilkårsvurder()
//            LeggTilSaksopplysningRespons(
//                behandling = nyBehandling,
//                erEndret = true,
//            )
//        }
        return LeggTilSaksopplysningRespons(
            behandling = this.copy(),
            erEndret = true,
        )
    }
}
