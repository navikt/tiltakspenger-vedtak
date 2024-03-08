package no.nav.tiltakspenger.saksbehandling.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.saksopplysning.lagVurdering
import no.nav.tiltakspenger.saksbehandling.vilkår.Utfall

sealed interface Søknadsbehandling : no.nav.tiltakspenger.saksbehandling.behandling.Behandling {
    val søknader: List<Søknad>

    fun søknad(): Søknad {
        return søknader.siste()
    }

    data class Opprettet(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val saksbehandler: String?,

    ) : Søknadsbehandling {
        companion object {
            fun fromDb(
                id: BehandlingId,
                sakId: SakId,
                søknader: List<Søknad>,
                vurderingsperiode: Periode,
                saksopplysninger: List<Saksopplysning>,
                tiltak: List<Tiltak>,
                saksbehandler: String?,
            ): Opprettet {
                return Opprettet(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    saksbehandler = saksbehandler,
                )
            }

            fun leggTilSøknad(behandling: Søknadsbehandling, søknad: Søknad): Opprettet {
                val fakta = if (søknad.vurderingsperiode() != behandling.vurderingsperiode) {
                    no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaInit(søknad) + no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaAvSøknad(
                        søknad,
                    )
                } else {
                    no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaAvSøknad(søknad)
                        .fold(behandling.saksopplysninger) { acc, saksopplysning ->
                            acc.oppdaterSaksopplysninger(saksopplysning)
                        }
                }

                return Opprettet(
                    id = behandling.id,
                    sakId = behandling.sakId,
                    søknader = behandling.søknader + søknad,
                    vurderingsperiode = søknad.vurderingsperiode(),
                    saksopplysninger = fakta,
                    tiltak = behandling.tiltak,
                    saksbehandler = behandling.saksbehandler,
                )
            }

            fun opprettBehandling(sakId: SakId, søknad: Søknad): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    sakId = sakId,
                    søknader = listOf(søknad),
                    vurderingsperiode = søknad.vurderingsperiode(),
                    saksopplysninger = no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaInit(søknad) + no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaAvSøknad(
                        søknad,
                    ),
                    tiltak = emptyList(),
                    saksbehandler = null,
                )
            }
        }

        override fun erÅpen() = true

        fun vilkårsvurder(): BehandlingVilkårsvurdert {
            // Først lager vi Vurderinger
            // todo Her kan vi vurdere å lage bare en map og ta som en forutsetning at det er en saksopplysning for hvert vilkår

            val vurderinger = saksopplysninger().flatMap {
                it.lagVurdering(vurderingsperiode)
            }

            // Etter at vi har laget vurderinger, sjekker vi utfallet

            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
                return BehandlingVilkårsvurdert.Manuell(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.OPPFYLT }) {
                return BehandlingVilkårsvurdert.Innvilget(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            if (vurderinger.all { it.utfall == Utfall.IKKE_OPPFYLT }) {
                return BehandlingVilkårsvurdert.Avslag(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vurderinger,
                    saksbehandler = saksbehandler,
                )
            }
            return BehandlingVilkårsvurdert.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vurderinger,
                saksbehandler = saksbehandler,
            )
        }

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Opprettet(
                id = id,
                sakId = sakId,
                søknader = søknader + søknad,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = no.nav.tiltakspenger.saksbehandling.behandling.lagFaktaAvSøknad(søknad)
                    .fold(saksopplysninger) { acc, saksopplysning ->
                        acc.oppdaterSaksopplysninger(saksopplysning)
                    },
                tiltak = tiltak,
                saksbehandler = saksbehandler,
            ).vilkårsvurder()
        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): no.nav.tiltakspenger.saksbehandling.behandling.LeggTilSaksopplysningRespons {
            val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
            return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
                no.nav.tiltakspenger.saksbehandling.behandling.LeggTilSaksopplysningRespons(
                    behandling = this,
                    erEndret = false,
                )
            } else {
                no.nav.tiltakspenger.saksbehandling.behandling.LeggTilSaksopplysningRespons(
                    behandling = this.copy(saksopplysninger = oppdatertSaksopplysningListe).vilkårsvurder(),
                    erEndret = true,
                )
            }
        }

        override fun oppdaterTiltak(tiltak: List<Tiltak>): Søknadsbehandling =
            this.copy(
                tiltak = tiltak,
            )

        override fun startBehandling(saksbehandler: String): Søknadsbehandling =
            this.copy(
                saksbehandler = saksbehandler,
            )

        override fun avbrytBehandling(): Søknadsbehandling =
            this.copy(
                saksbehandler = null,
            )
    }
}
