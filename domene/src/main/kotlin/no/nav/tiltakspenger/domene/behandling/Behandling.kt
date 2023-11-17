package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

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

    fun leggTilSøknad(søknad: Søknad): BehandlingVilkårsvurdert {
        throw IllegalStateException("Kan ikke legge til søknad på denne behandlingen")
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): Søknadsbehandling {
        throw IllegalStateException("Kan ikke oppdatere tiltak på denne behandlingen")
    }

    fun taBahandling(saksbehandler: String): Søknadsbehandling {
        throw IllegalStateException("Kan ikke ta en behandling med denne statusen")
    }
}

fun List<Saksopplysning>.oppdaterSaksopplysninger(saksopplysning: Saksopplysning) =
    if (saksopplysning.kilde != Kilde.SAKSB) {
        if (this.first { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB } == saksopplysning) {
            this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB }
        } else {
            this.filterNot { it.vilkår == saksopplysning.vilkår }
        }
    } else {
        this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde == Kilde.SAKSB }
    }.plus(saksopplysning)
