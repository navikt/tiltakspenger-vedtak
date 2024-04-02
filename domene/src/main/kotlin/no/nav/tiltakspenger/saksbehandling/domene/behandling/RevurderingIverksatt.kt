package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class RevurderingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val forrigeVedtak: Vedtak,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode>,
    override val søknader: List<Søknad>,
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String,
    val status: BehandlingStatus,
) : Revurderingsbehandling {

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            val nyBehandling = RevurderingOpprettet(
                id = BehandlingId.random(),
                sakId = this.sakId,
                søknader = listOf(this.søknad()),
                vurderingsperiode = this.vurderingsperiode,
                saksopplysninger = oppdatertSaksopplysningListe,
                tiltak = this.tiltak,
                saksbehandler = null,
                forrigeVedtak = this.forrigeVedtak,
            ).vilkårsvurder()
            LeggTilSaksopplysningRespons(
                behandling = nyBehandling,
                erEndret = true,
            )
        }
    }
}
