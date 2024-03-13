package no.nav.tiltakspenger.domene.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class BehandlingVilkårsvurdert(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val status: BehandlingStatus,
    val vilkårsvurderinger: List<Vurdering>,
) : Førstegangsbehandling {

    fun vurderPåNytt(): BehandlingVilkårsvurdert {
        return BehandlingOpprettet(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            saksbehandler = saksbehandler,
        ).vilkårsvurder()
    }

    fun iverksett(): BehandlingIverksatt {
        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke iverksette denne behandlingen")
            else ->
                BehandlingIverksatt(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    utfallsperioder = utfallsperioder,
                    saksbehandler = "Automatisk",
                    beslutter = "Automatisk",
                    status = status,
                )
        }
    }

    fun tilBeslutting(): BehandlingTilBeslutter {
        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("Kan ikke sende denne behandlingen til beslutter")
            else -> BehandlingTilBeslutter(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = checkNotNull(saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" },
                beslutter = null,
                status = status,
            )
        }
    }

    override fun erÅpen() = true

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        return BehandlingOpprettet.leggTilSøknad(
            behandling = this,
            søknad = søknad,
        ).vilkårsvurder()
    }

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

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling =
        this.copy(tiltak = tiltak)

    override fun startBehandling(saksbehandler: String): Førstegangsbehandling =
        this.copy(saksbehandler = saksbehandler)

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }

    companion object {
        fun fromDb(
            id: BehandlingId,
            sakId: SakId,
            søknader: List<Søknad>,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            tiltak: List<Tiltak>,
            vilkårsvurderinger: List<Vurdering>,
            status: String,
            saksbehandler: String?,
            utfallsperioder: List<Utfallsperiode>,
        ): BehandlingVilkårsvurdert {
            val behandlingVilkårsvurdertStatus = when (status) {
                "Innvilget" -> BehandlingStatus.Innvilget
                "Avslag" -> BehandlingStatus.Avslag
                "Manuell" -> BehandlingStatus.Manuell
                else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
            }
            return BehandlingVilkårsvurdert(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                utfallsperioder = utfallsperioder,
                status = behandlingVilkårsvurdertStatus,
            )
        }
    }
}
