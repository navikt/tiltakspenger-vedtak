package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class BehandlingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val livsoppholdVilkårData: LivsoppholdVilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>?,
    val beslutter: String,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    override fun leggTilSaksopplysning(livoppholdSaksopplysning: LivoppholdSaksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertYtelserVilkårData = livsoppholdVilkårData.oppdaterSaksopplysninger(livoppholdSaksopplysning)
        return if (oppdatertYtelserVilkårData == this.livsoppholdVilkårData) {
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
                livsoppholdVilkårData = oppdatertYtelserVilkårData,
                tiltak = this.tiltak,
                saksbehandler = null,
            ).vilkårsvurder()
            LeggTilSaksopplysningRespons(
                behandling = nyBehandling,
                erEndret = true,
            )
        }
    }
}
