package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

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

    fun iverksett(utøvendeBeslutter: Saksbehandler): BehandlingIverksatt {
        // checkNotNull(saksbehandler) { "Kan ikke iverksette en behandling uten saksbehandler" }
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        check(utøvendeBeslutter.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }
        check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }

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

    fun sendTilbake(utøvendeBeslutter: Saksbehandler): BehandlingVilkårsvurdert {
        check(utøvendeBeslutter.isBeslutter() || utøvendeBeslutter.isAdmin()) { "Saksbehandler må være beslutter eller administrator" }
        check(this.beslutter == utøvendeBeslutter.navIdent || utøvendeBeslutter.isAdmin()) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
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

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.beslutter == null) { "Denne behandlingen har allerede en beslutter" }
        check(saksbehandler.isBeslutter()) { "Saksbehandler må være beslutter" }
        return this.copy(
            beslutter = saksbehandler.navIdent,
        )
    }

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
}
