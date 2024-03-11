package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler

data class LeggTilSaksopplysningRespons(
    val behandling: Søknadsbehandling,
    val erEndret: Boolean,
)

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>
    val tiltak: List<Tiltak>
    val saksbehandler: String?

    fun saksopplysninger(): List<Saksopplysning> {
        return saksopplysninger.groupBy { it.vilkår }.map { entry ->
            entry.value.reduce { acc, saksopplysning ->
                if (saksopplysning.kilde == Kilde.SAKSB) saksopplysning else acc
            }
        }
    }

    fun erÅpen(): Boolean = false
    fun erIverksatt(): Boolean = false
    fun erTilBeslutter(): Boolean = false

    fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        throw IllegalStateException("Kan ikke legge til søknad på denne behandlingen")
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): Søknadsbehandling {
        throw IllegalStateException("Kan ikke oppdatere tiltak på denne behandlingen")
    }

    fun startBehandling(saksbehandler: String): Søknadsbehandling {
        throw IllegalStateException("Kan ikke starte en behandling med denne statusen")
    }

    fun avbrytBehandling(saksbehandler: Saksbehandler): Søknadsbehandling {
        throw IllegalStateException("Kan ikke avbryte en behandling med denne statusen")
    }
}
