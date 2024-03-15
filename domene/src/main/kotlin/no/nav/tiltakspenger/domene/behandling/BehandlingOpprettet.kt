package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler

data class BehandlingOpprettet(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val søknader: List<Søknad>,
    override val vurderingsperiode: Periode,
    override val saksopplysninger: List<Saksopplysning>,
    override val tiltak: List<Tiltak>,
    override val saksbehandler: String?,
    override val utfallsperioder: List<Utfallsperiode> = emptyList(),
) : Førstegangsbehandling {

    companion object {
        // TODO: Hva er forskjellen på leggTilSøknad og opprettBehandling?
        fun leggTilSøknad(behandling: Førstegangsbehandling, søknad: Søknad): BehandlingOpprettet {
            val fakta = if (søknad.vurderingsperiode() != behandling.vurderingsperiode) {
                Saksopplysninger.initSaksopplysningerFraSøknad(søknad) + Saksopplysninger.lagSaksopplysningerAvSøknad(
                    søknad,
                )
            } else {
                Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                    .fold(behandling.saksopplysninger) { acc, saksopplysning ->
                        acc.oppdaterSaksopplysninger(saksopplysning)
                    }
            }

            return BehandlingOpprettet(
                id = behandling.id,
                sakId = behandling.sakId,
                søknader = behandling.søknader + søknad,
                vurderingsperiode = søknad.vurderingsperiode(),
                saksopplysninger = fakta,
                tiltak = behandling.tiltak,
                saksbehandler = behandling.saksbehandler,
            )
        }

        fun opprettBehandling(sakId: SakId, søknad: Søknad): BehandlingOpprettet {
            return BehandlingOpprettet(
                id = BehandlingId.random(),
                sakId = sakId,
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
                saksopplysninger = Saksopplysninger.initSaksopplysningerFraSøknad(søknad) + Saksopplysninger.lagSaksopplysningerAvSøknad(
                    søknad,
                ),
                tiltak = emptyList(),
                saksbehandler = null,
            )
        }
    }

    override fun erÅpen() = true

    // TODO: Hva er forskjellen på denne og på leggTilSøknad (og opprettBehandling) lenger opp?
    // Det virker som om det er for mange innganger til klassen..?
    override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        return BehandlingOpprettet(
            id = id,
            sakId = sakId,
            søknader = søknader + søknad,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
                .fold(saksopplysninger) { acc, saksopplysning ->
                    acc.oppdaterSaksopplysninger(saksopplysning)
                },
            tiltak = tiltak,
            saksbehandler = saksbehandler,
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
                behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vilkårsvurder(),
                erEndret = true,
            )
        }
    }

    override fun oppdaterTiltak(tiltak: List<Tiltak>): Førstegangsbehandling =
        this.copy(
            tiltak = tiltak,
        )

    override fun startBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(this.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(
            saksbehandler = saksbehandler.navIdent,
        )
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(
            saksbehandler = null,
        )
    }
}
