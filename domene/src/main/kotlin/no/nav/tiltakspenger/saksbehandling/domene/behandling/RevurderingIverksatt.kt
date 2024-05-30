package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class RevurderingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val livsoppholdVilkårData: LivsoppholdVilkårData,
    override val tiltak: List<Tiltak>,
    override val forrigeVedtak: Vedtak,
    override val saksbehandler: String,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>?,
    override val søknader: List<Søknad>,
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String,
    val status: BehandlingStatus,
) : Revurderingsbehandling {

    override fun leggTilSaksopplysning(livoppholdSaksopplysning: LivoppholdSaksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertYtelserVilkårData = livsoppholdVilkårData.oppdaterSaksopplysninger(livoppholdSaksopplysning)
        return if (oppdatertYtelserVilkårData == this.livsoppholdVilkårData) {
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
                livsoppholdVilkårData = livsoppholdVilkårData,
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
