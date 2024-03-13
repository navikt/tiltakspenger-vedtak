package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

data class BehandlingTilBeslutter(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
    val vilkårsvurderinger: List<Vurdering>,
    val beslutter: String?,
    val status: BehandlingStatus,
) : Førstegangsbehandling {

    override fun erTilBeslutter() = true

    fun iverksett(): BehandlingIverksatt {
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("En behandling til beslutter kan ikke være manuell")
            BehandlingStatus.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 ${this.id}")
            else -> BehandlingIverksatt(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                utfallsperioder = utfallsperioder,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
                status = status,
            )
        }
    }

    fun sendTilbake(): BehandlingVilkårsvurdert {
        return BehandlingVilkårsvurdert(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            vilkårsvurderinger = vilkårsvurderinger,
            utfallsperioder = utfallsperioder,
            saksbehandler = saksbehandler,
            status = BehandlingStatus.Innvilget,
        )
    }

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

    override fun startBehandling(saksbehandler: String): Førstegangsbehandling =
        this.copy(
            beslutter = saksbehandler,
        )

    private fun vurderPåNytt(): BehandlingVilkårsvurdert {
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
            saksbehandler: String,
            beslutter: String?,
            utfallsperioder: List<Utfallsperiode>,
        ): BehandlingTilBeslutter {
            val behandlingStatus = when (status) {
                "Innvilget" -> BehandlingStatus.Innvilget
                "Avslag" -> BehandlingStatus.Avslag
                else -> throw IllegalStateException("Ukjent BehandlingTilBeslutting $id med status $status")
            }
            return BehandlingTilBeslutter(
                id = id,
                sakId = sakId,
                søknader = søknader,
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
