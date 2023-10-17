package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

interface Behandling {
    val id: BehandlingId
    val sakId: SakId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>

    fun saksopplysninger(): List<Saksopplysning> {
        val saksopplysningerMap = saksopplysninger.groupBy { it.vilkår }
        val liste = mutableListOf<Saksopplysning>()

        for ((_, saksopplysninger) in saksopplysningerMap) {
            val saksopplysning = saksopplysninger.find { it.kilde == Kilde.SAKSB }
                ?: saksopplysninger.firstOrNull()

            saksopplysning?.let {
                liste.add(it)
            }
        }
        return liste
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }
}
