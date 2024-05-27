package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedKildeOgVerdi
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning

data class LeggTilSaksopplysningRespons(
    val behandling: Behandling,
    val erEndret: Boolean,
)

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>
    val tiltak: List<Tiltak>
    val saksbehandler: String?
    val utfallsperioder: List<Utfallsperiode>
    val søknader: List<Søknad>

    fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)

    fun saksopplysninger(): List<Saksopplysning> {
        return saksopplysninger.groupBy { it.vilkår }.map { entry ->
            entry.value.reduce { acc, saksopplysning ->
                if (saksopplysning.kilde == Kilde.SAKSB) saksopplysning else acc
            }
        }
    }

    fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        throw IllegalStateException("Kan ikke legge til søknad på denne behandlingen")
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): Behandling {
        throw IllegalStateException("Kan ikke oppdatere tiltak på denne behandlingen")
    }

    fun startBehandling(saksbehandler: Saksbehandler): Behandling {
        throw IllegalStateException("Kan ikke starte en behandling med denne statusen")
    }

    fun avbrytBehandling(saksbehandler: Saksbehandler): Behandling {
        throw IllegalStateException("Kan ikke avbryte en behandling med denne statusen")
    }

    fun oppdaterAntallDager(tiltakId: String, verdier: List<PeriodeMedKildeOgVerdi<Int>>, saksbehandler: Saksbehandler): Behandling {
        throw IllegalStateException("Kan ikke oppdatere antall dager på denne behandlingen")
    }
}
