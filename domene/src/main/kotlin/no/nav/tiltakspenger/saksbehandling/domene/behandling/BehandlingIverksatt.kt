package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.exceptions.StøtterIkkeException
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.VilkårDataYtelser

data class BehandlingIverksatt(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val vilkårDatumYtelsers: List<VilkårDataYtelser>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val beslutter: String,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        throw StøtterIkkeException("Denne funksjonaliteten er ikke støttet enda. Se på i forlengelse av 'lytte på fakta'-oppgaven")
    }
}
