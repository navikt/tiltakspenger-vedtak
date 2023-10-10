package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

//    fun toDTO(): BehandlingDTO {
//        throw IllegalStateException("Kan ikke gjøres om til en DTO")
//    }
}
