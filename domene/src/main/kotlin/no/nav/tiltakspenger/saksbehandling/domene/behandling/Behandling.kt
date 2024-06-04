package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class LeggTilSaksopplysningRespons(
    val behandling: Behandling,
    val erEndret: Boolean,
)

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val livsoppholdVilkårData: LivsoppholdVilkårData
    val tiltak: List<Tiltak>
    val saksbehandler: String?
    val utfallsperioder: Periodisering<Utfallsdetaljer>?
    val søknader: List<Søknad>

    fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)

    fun saksopplysninger(): List<LivsoppholdSaksopplysning> {
        return livsoppholdVilkårData.korrigerbareYtelser.values.map {
            it.avklartLivsoppholdSaksopplysning
        }
    }

    fun vilkårsvurderinger(): List<Vurdering> {
        return livsoppholdVilkårData.korrigerbareYtelser.values.map {
            it.vurdering
        }
    }

    fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        throw IllegalStateException("Kan ikke legge til søknad på denne behandlingen")
    }

    fun leggTilSaksopplysning(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): LeggTilSaksopplysningRespons {
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
}
