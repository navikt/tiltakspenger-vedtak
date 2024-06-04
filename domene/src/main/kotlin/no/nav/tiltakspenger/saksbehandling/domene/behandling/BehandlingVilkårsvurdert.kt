package no.nav.tiltakspenger.saksbehandling.domene.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdVilkårData

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class BehandlingVilkårsvurdert(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val livsoppholdVilkårData: LivsoppholdVilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>? = null,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    fun tilBeslutting(saksbehandler: Saksbehandler): BehandlingTilBeslutter {
        checkNotNull(this.saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" }
        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke sende denne behandlingen til beslutter")
            else -> BehandlingTilBeslutter(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                livsoppholdVilkårData = livsoppholdVilkårData,
                tiltak = tiltak,
                utfallsperioder = utfallsperioder,
                saksbehandler = this.saksbehandler,
                beslutter = null,
                status = status,
            )
        }
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert =
        this.spolTilbake().leggTilSøknad(søknad = søknad)

    override fun leggTilSaksopplysning(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): LeggTilSaksopplysningRespons =
        this.spolTilbake().leggTilSaksopplysning(livsoppholdSaksopplysning)

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling =
        this.copy(tiltak = tiltak)

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }

    fun spolTilbake(): BehandlingOpprettet = BehandlingOpprettet(
        id = this.id,
        sakId = this.sakId,
        søknader = this.søknader,
        vurderingsperiode = this.vurderingsperiode,
        livsoppholdVilkårData = this.livsoppholdVilkårData,
        tiltak = this.tiltak,
        saksbehandler = this.saksbehandler,
    )
}
