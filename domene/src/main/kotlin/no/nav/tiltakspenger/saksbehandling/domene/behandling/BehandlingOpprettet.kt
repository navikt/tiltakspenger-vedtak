package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelserVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class BehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val ytelserVilkårData: YtelserVilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: Periodisering<Utfallsdetaljer>? = null,
) : Førstegangsbehandling {

    companion object {

        fun opprettBehandling(sakId: SakId, søknad: Søknad): BehandlingOpprettet {
            return BehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
                ytelserVilkårData = YtelserVilkårData(søknad.vurderingsperiode()).håndterSøknad(søknad),
                tiltak = emptyList(),
                saksbehandler = null,
            )
        }
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        // TODO: Egentlig har vi ikke definert godt nok hva vi skal gjøre i disse tilfellene
        return this.copy(
            ytelserVilkårData = ytelserVilkårData.håndterSøknad(søknad),
        ).vilkårsvurder()
        /*
        val fakta = if (søknad.vurderingsperiode() != this.vurderingsperiode) {
            Saksopplysninger.initSaksopplysningerFraSøknad(søknad) +
                Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
        } else {
            Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                .fold(this.saksopplysninger) { acc, saksopplysning ->
                    acc.oppdaterSaksopplysninger(saksopplysning)
                }
        }

        return this.copy(
            søknader = this.søknader + søknad,
            vurderingsperiode = søknad.vurderingsperiode(),
            saksopplysninger = fakta,
        ).vilkårsvurder()

         */
    }

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

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling =
        this.copy(tiltak = tiltak).vilkårsvurder()

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }
}
