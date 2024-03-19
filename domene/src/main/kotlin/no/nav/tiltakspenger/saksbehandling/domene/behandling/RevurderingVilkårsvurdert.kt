package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class RevurderingVilkårsvurdert(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val forrigeVedtak: Vedtak,
    override val utfallsperioder: List<Utfallsperiode>,
    override val søknader: List<Søknad>,
    val status: BehandlingStatus,
    val vilkårsvurderinger: List<Vurdering>,
) : Revurderingsbehandling {

    fun vurderPåNytt(): RevurderingVilkårsvurdert {
        return RevurderingOpprettet(
            id = id,
            sakId = sakId,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            saksbehandler = saksbehandler,
            forrigeVedtak = forrigeVedtak,
            søknader = søknader,
        ).vilkårsvurder()
    }

    fun iverksett(): RevurderingIverksatt {
        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke iverksette denne behandlingen")
            else ->
                RevurderingIverksatt(
                    id = id,
                    sakId = sakId,
                    forrigeVedtak = forrigeVedtak,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    utfallsperioder = utfallsperioder,
                    saksbehandler = "Automatisk",
                    beslutter = "Automatisk",
                    status = status,
                    søknader = søknader,
                )
        }
    }

    fun tilBeslutting(saksbehandler: Saksbehandler): RevurderingTilBeslutter {
        checkNotNull(this.saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" }
        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke sende denne behandlingen til beslutter")
            else -> RevurderingTilBeslutter(
                id = id,
                sakId = sakId,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = this.saksbehandler,
                beslutter = null,
                status = status,
                forrigeVedtak = forrigeVedtak,
                søknader = søknader,
            )
        }
    }

    override fun erÅpen() = true

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vurderPåNytt(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Revurderingsbehandling =
        this.copy(tiltak = tiltak)

    override fun startBehandling(saksbehandler: Saksbehandler): Revurderingsbehandling {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Revurderingsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }
}
