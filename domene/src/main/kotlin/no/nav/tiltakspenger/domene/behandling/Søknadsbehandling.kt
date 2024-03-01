package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

sealed interface Søknadsbehandling : Behandling {
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
                    lagFaktaInit(søknad) + lagFaktaAvSøknad(søknad)
                } else {
                    lagFaktaAvSøknad(søknad).fold(behandling.saksopplysninger) { acc, saksopplysning ->
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
                    saksopplysninger = lagFaktaInit(søknad) + lagFaktaAvSøknad(søknad),
                    tiltak = emptyList(),
                    saksbehandler = null,
                )
            }
        }

        override fun erÅpen() = true

        override fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
            return Opprettet(
                id = id,
                sakId = sakId,
                søknader = søknader + søknad,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = lagFaktaAvSøknad(søknad).fold(saksopplysninger) { acc, saksopplysning ->
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
