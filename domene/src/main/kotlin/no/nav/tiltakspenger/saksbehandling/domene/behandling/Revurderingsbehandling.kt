package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class Revurderingsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val vurderingsperiode: Periode,
    override val søknader: List<Søknad>,
    override val saksbehandler: String?,
    override val beslutter: String?,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val vilkårsvurderinger: List<Vurdering>,
    override val utfallsperioder: List<Utfallsperiode>,
    override val status: BehandlingStatus,
    override val tilstand: BehandlingTilstand,
    val forrigeVedtak: Vedtak,
) : Behandling {

    companion object {
        fun opprettRevurderingsbehandling(vedtak: Vedtak, navIdent: String): Revurderingsbehandling {
            return Revurderingsbehandling(
                id = BehandlingId.random(),
                sakId = vedtak.sakId,
                forrigeVedtak = vedtak,
                vurderingsperiode = vedtak.periode,
                saksopplysninger = vedtak.saksopplysninger,
                tiltak = vedtak.behandling.tiltak,
                saksbehandler = navIdent,
                søknader = vedtak.behandling.søknader,
                beslutter = null,
                status = BehandlingStatus.Manuell,
                tilstand = BehandlingTilstand.OPPRETTET,
                utfallsperioder = emptyList(),
                vilkårsvurderinger = emptyList(),
            )
        }
    }

    override fun leggTilSøknad(søknad: Søknad): Behandling {
        TODO("Not yet implemented")
    }

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        TODO("Not yet implemented")
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Behandling {
        TODO("Not yet implemented")
    }

    override fun startBehandling(saksbehandler: Saksbehandler): Behandling {
        TODO("Not yet implemented")
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Behandling {
        TODO("Not yet implemented")
    }

    override fun tilBeslutting(saksbehandler: Saksbehandler): Behandling {
        TODO("Not yet implemented")
    }

    override fun iverksett(utøvendeBeslutter: Saksbehandler): Behandling {
        TODO("Not yet implemented")
    }

    override fun sendTilbake(utøvendeBeslutter: Saksbehandler): Behandling {
        TODO("Not yet implemented")
    }

    override fun vilkårsvurder(): Behandling {
        TODO("Not yet implemented")
    }

    override fun saksopplysninger(): List<Saksopplysning> {
        TODO("Not yet implemented")
    }
}
