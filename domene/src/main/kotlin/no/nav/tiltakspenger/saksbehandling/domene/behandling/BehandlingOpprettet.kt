package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.OppfyllbarVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

data class BehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val vilkårData: List<OppfyllbarVilkårData> = emptyList(),
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),

) : Førstegangsbehandling {

    companion object {
        private fun initVilkårData(vurderingsperiode: Periode): List<OppfyllbarVilkårData> {
            val vilkår = listOf(Vilkår.AAP, Vilkår.DAGPENGER, Vilkår.ALDER, Vilkår.SØKNADSFRIST, Vilkår.BARNETILLEGG, Vilkår.KVP)
            return vilkår.map {
                OppfyllbarVilkårData(
                    vilkår = it,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysningerSaksbehandler = emptyList(),
                    saksopplysningerAnnet = emptyList(),
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
                vilkårData = initVilkårData(vurderingsperiode = vurderingsperiode),
                saksopplysninger = emptyList(),
            )

            return opprettetBehandling
        }
    }

    fun lagSaksopplysningerFraSøknad(søknad: Søknad): BehandlingOpprettet {
        val saksopplysningerFraSøknaden = Saksopplysninger.lagSaksopplysningerAvSøknad(søknad = søknad)
        val vilkårData = saksopplysningerFraSøknaden.map {

            // TODO Vi må ta hensyn til at det kommer en ny søknad, og at da ikke søknadstildspunkt overskrives
            OppfyllbarVilkårData(
                vilkår = it.vilkår,
                vurderingsperiode = this.vurderingsperiode,
                saksopplysningerSaksbehandler = emptyList(),
                saksopplysningerAnnet = listOf(),
                vurderinger = emptyList(),
            )
        }

        return this.copy(
            vilkårData = vilkårData
        )
    }

    override fun leggTilSaksopplysningerForSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        val behandlingOpprettet = //if (søknad.vurderingsperiode() != this.vurderingsperiode) {
            lagSaksopplysningerFraSøknad(søknad)
//        }
        // else {
            // Hvis saksopplysningene skal legges til på en eksisterende behandling
        // }


        return behandlingOpprettet.vilkårsvurder()
    }

    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        val fakta = if (søknad.vurderingsperiode() != this.vurderingsperiode) {
            Saksopplysninger.initSaksopplysningerFraSøknad(søknad) +
                Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
        } else {
            Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                .fold(this.avklarteSaksopplysninger) { acc, saksopplysning ->
                    acc.oppdaterSaksopplysninger(saksopplysning)
                }
        }

        return this.copy(
            søknader = this.søknader + søknad,
            vurderingsperiode = søknad.vurderingsperiode(),
            saksopplysninger = fakta,
        ).vilkårsvurder()
    }

    override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        val oppdatertSaksopplysningListe = avklarteSaksopplysninger.oppdaterSaksopplysninger(saksopplysning)
        return if (oppdatertSaksopplysningListe == this.avklarteSaksopplysninger) {
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
