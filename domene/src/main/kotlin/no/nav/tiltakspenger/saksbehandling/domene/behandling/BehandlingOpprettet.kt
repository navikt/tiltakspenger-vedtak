package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.SaksopplysningInterface
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.VilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.YtelseVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class BehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val vilkårData: VilkårData,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),

    ) : Førstegangsbehandling {

    companion object {
        private fun initVilkårData(vurderingsperiode: Periode): List<YtelseVilkårData> {
            val vilkår =
                listOf(Vilkår.AAP, Vilkår.DAGPENGER, Vilkår.ALDER, Vilkår.SØKNADSFRIST, Vilkår.BARNETILLEGG, Vilkår.KVP)
            return vilkår.map {
                YtelseVilkårData(
                    vilkår = it,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysningerSaksbehandler = null,
                    saksopplysningerAnnet = null,
                    avklarteSaksopplysninger = null,
                    vurderinger = emptyList(),
                )
            }
        }

        fun opprettBehandling(sakId: SakId, søknad: Søknad): BehandlingOpprettet {
            val vurderingsperiode = søknad.vurderingsperiode()
            val opprettetBehandling = BehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = vurderingsperiode,
                tiltak = emptyList(),
                saksbehandler = null,
                vilkårData = VilkårData.opprettFraSøknad(søknad),
            )

            return opprettetBehandling
        }
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {

        val vilkårData = if(søknad.vurderingsperiode() == this.vurderingsperiode) {
            vilkårData.leggTilSøknad(søknad)
        } else {
            VilkårData.opprettFraSøknad(søknad)
        }

        return this.copy(
            vurderingsperiode = søknad.vurderingsperiode(),
            søknader = this.søknader + søknad,
            vilkårData = vilkårData
        ).vilkårsvurder()
    }

    override fun leggTilSaksopplysning(saksopplysning: SaksopplysningInterface): BehandlingVilkårsvurdert =
        this.copy(vilkårData = vilkårData.leggTilSaksopplysning(saksopplysning)).vilkårsvurder()

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
}
