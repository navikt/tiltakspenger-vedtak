package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler

data class RevurderingBehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val forrigeBehandling: Førstegangsbehandling,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    ) : Revurderingsbehandling {
    companion object {
        fun fromDb(
            id: BehandlingId,
            sakId: SakId,
            forrigeBehandling: Førstegangsbehandling,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            tiltak: List<Tiltak>,
            saksbehandler: String?,
        ): RevurderingBehandlingOpprettet {
            return RevurderingBehandlingOpprettet(
                id = id,
                sakId = sakId,
                forrigeBehandling = forrigeBehandling,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                saksbehandler = saksbehandler,
            )
        }

        fun opprettRevurderingsbehandling(behandlingIverksatt: BehandlingIverksatt): RevurderingBehandlingOpprettet {
            return RevurderingBehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = behandlingIverksatt.sakId,
                forrigeBehandling = behandlingIverksatt,
                vurderingsperiode = behandlingIverksatt.vurderingsperiode,
                saksopplysninger = behandlingIverksatt.saksopplysninger,
                tiltak = behandlingIverksatt.tiltak,
                saksbehandler = null,
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
                behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): RevurderingBehandlingOpprettet =
        this.copy(
            tiltak = tiltak,
        )

    override fun startBehandling(saksbehandler: Saksbehandler): RevurderingBehandlingOpprettet {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(
            saksbehandler = saksbehandler.navIdent,
        )
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): RevurderingBehandlingOpprettet {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(
            saksbehandler = null,
        )
    }
}
