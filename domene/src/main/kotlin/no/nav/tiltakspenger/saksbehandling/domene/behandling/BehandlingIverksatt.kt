package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class BehandlingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            // todo() her må vi lage en revurdering
            val nyBehandling = BehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = this.sakId,
                søknader = listOf(this.søknad()),
                vurderingsperiode = this.vurderingsperiode,
                saksopplysninger = oppdatertSaksopplysningListe,
                tiltak = this.tiltak,
                saksbehandler = null,
                utfallsperioder = emptyList(), // TODO: Denne må endres til riktig verdi!! Gjør ikke dette helt enda siden det er litt lengre oppi veien
                ytelsessaksopplysninger = initYtelsesopplysninger(this.vurderingsperiode), // TODO: Denne må endres til riktig verdi!! Gjør ikke dette helt enda siden det er litt lengre oppi veien
            ).vilkårsvurder()
            LeggTilSaksopplysningRespons(
                behandling = nyBehandling,
                erEndret = true,
            )
        }
    }
}
