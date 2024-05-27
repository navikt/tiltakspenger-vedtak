package no.nav.tiltakspenger.saksbehandling.domene.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.barnetillegg.BarnetilleggVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class BehandlingVilkårsvurdert(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val barnetillegg: BarnetilleggVilkårData,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val vilkårsvurderinger: List<Vurdering>,
) : Førstegangsbehandling {

    private fun status(): BehandlingStatus =
        if (utfallsperioder.any { it.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
            BehandlingStatus.Manuell
        } else if (utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
            BehandlingStatus.Innvilget
        } else {
            BehandlingStatus.Avslag
        }

    // TODO: Denne kalles aldri, og er det egentlig lov å gå direkte fra Vilkårsvurdert til Ivkersatt?
    fun iverksett(): BehandlingIverksatt {
        return when (status()) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke iverksette denne behandlingen")
            else ->
                BehandlingIverksatt(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    barnetillegg = barnetillegg,
                    vilkårsvurderinger = vilkårsvurderinger,
                    utfallsperioder = utfallsperioder,
                    saksbehandler = "Automatisk",
                    beslutter = "Automatisk",
                    status = status(),
                )
        }
    }

    fun tilBeslutting(saksbehandler: Saksbehandler): BehandlingTilBeslutter {
        checkNotNull(this.saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" }
        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }

        return when (status()) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke sende denne behandlingen til beslutter")
            else -> BehandlingTilBeslutter(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                barnetillegg = barnetillegg,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = this.saksbehandler,
                beslutter = null,
                status = status(),
            )
        }
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert =
        this.spolTilbake().leggTilSøknad(søknad = søknad)

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons =
        this.spolTilbake().leggTilSaksopplysning(saksopplysning)

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
        saksopplysninger = this.saksopplysninger,
        tiltak = this.tiltak,
        barnetillegg = barnetillegg,
        saksbehandler = this.saksbehandler,
    )
}
