package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelserVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class RevurderingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val forrigeVedtak: Vedtak,
    override val vurderingsperiode: Periode,
    override val ytelserVilkårData: YtelserVilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val søknader: List<Søknad>,
) : Revurderingsbehandling {
    companion object {
        fun opprettRevurderingsbehandling(vedtak: Vedtak, navIdent: String): RevurderingOpprettet {
            return RevurderingOpprettet(
                id = BehandlingId.random(),
                sakId = vedtak.sakId,
                forrigeVedtak = vedtak,
                vurderingsperiode = vedtak.periode,
                ytelserVilkårData = vedtak.behandling.ytelserVilkårData,
                tiltak = vedtak.behandling.tiltak,
                saksbehandler = navIdent,
                søknader = vedtak.behandling.søknader,
            )
        }
    }

    override val utfallsperioder: Periodisering<Utfallsdetaljer>
        get() = TODO("Not yet implemented")

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertYtelserVilkårData = ytelserVilkårData.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertYtelserVilkårData == this.ytelserVilkårData) {
            LeggTilSaksopplysningRespons(
                behandling = this,
                erEndret = false,
            )
        } else {
            LeggTilSaksopplysningRespons(
                behandling = this.copy(ytelserVilkårData = oppdatertYtelserVilkårData).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): RevurderingOpprettet =
        this.copy(tiltak = tiltak)

    override fun startBehandling(saksbehandler: Saksbehandler): RevurderingOpprettet {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(
            saksbehandler = saksbehandler.navIdent,
        )
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): RevurderingOpprettet {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(
            saksbehandler = null,
        )
    }
}
