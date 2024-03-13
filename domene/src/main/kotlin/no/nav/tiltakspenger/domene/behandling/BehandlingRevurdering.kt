package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler

sealed interface BehandlingRevurdering : Behandling {
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

            fun opprettRevurderingsbehandling(behandlingIverksatt: BehandlingIverksatt): Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    sakId = behandlingIverksatt.sakId,
                    søknader = behandlingIverksatt.søknader,
                    vurderingsperiode = behandlingIverksatt.vurderingsperiode,
                    saksopplysninger = behandlingIverksatt.saksopplysninger,
                    tiltak = behandlingIverksatt.tiltak,
                    saksbehandler = null,
                )
            }
        }

        override fun erÅpen() = true

        override fun oppdaterTiltak(tiltak: List<Tiltak>): Søknadsbehandling =
            this.copy(
                tiltak = tiltak,
            )

        override fun startBehandling(saksbehandler: String): Søknadsbehandling =
            this.copy(
                saksbehandler = saksbehandler,
            )

        override fun avbrytBehandling(saksbehandler: Saksbehandler): Søknadsbehandling {
            check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
            return this.copy(
                saksbehandler = null,
            )
        }
    }
}
