package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Endringslogg
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class RevurderingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val forrigeVedtak: Vedtak,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val søknader: List<Søknad>,
    override val endringslogg: Endringslogg,
) : Revurderingsbehandling {
    companion object {
        fun opprettRevurderingsbehandling(vedtak: Vedtak, navIdent: String): RevurderingOpprettet {
            val behandlingId = BehandlingId.random()
            return RevurderingOpprettet(
                id = behandlingId,
                sakId = vedtak.sakId,
                forrigeVedtak = vedtak,
                vurderingsperiode = vedtak.periode,
                saksopplysninger = vedtak.saksopplysninger,
                tiltak = vedtak.behandling.tiltak,
                saksbehandler = navIdent,
                søknader = vedtak.behandling.søknader,
                endringslogg = Endringslogg(vedtak.sakId, behandlingId),
            )
        }
    }

    override val utfallsperioder: List<Utfallsperiode>
        get() = TODO("Not yet implemented")

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
