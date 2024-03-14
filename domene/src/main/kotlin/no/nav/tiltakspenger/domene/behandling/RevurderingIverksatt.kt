package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

data class RevurderingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val forrigeBehandling: Førstegangsbehandling,
    override val saksbehandler: String,
    val vilkårsvurderinger: List<Vurdering>,
    val utfallsperioder: List<Utfallsperiode>,
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
            val nyBehandling = RevurderingOpprettet.fromDb(
                id = BehandlingId.random(),
                sakId = this.sakId,
                vurderingsperiode = this.vurderingsperiode,
                saksopplysninger = oppdatertSaksopplysningListe,
                tiltak = this.tiltak,
                saksbehandler = null,
                forrigeBehandling = forrigeBehandling,
            ).vilkårsvurder()
            LeggTilSaksopplysningRespons(
                behandling = nyBehandling,
                erEndret = true,
            )
        }
    }

    override fun erTilBeslutter() = true

    companion object {
        fun fromDb(
            id: BehandlingId,
            sakId: SakId,
            forrigeBehandling: Førstegangsbehandling,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            tiltak: List<Tiltak>,
            vilkårsvurderinger: List<Vurdering>,
            utfallsperioder: List<Utfallsperiode>,
            status: String,
            saksbehandler: String,
            beslutter: String,
        ): RevurderingIverksatt {
            val behandlingStatus = when (status) {
                "Innvilget" -> BehandlingStatus.Innvilget
                "Avslag" -> BehandlingStatus.Avslag
                else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
            }
            return RevurderingIverksatt(
                id = id,
                sakId = sakId,
                forrigeBehandling = forrigeBehandling,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
                status = behandlingStatus,
            )
        }
    }
}
