package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

interface Behandling {
    val id: BehandlingId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
        throw IllegalStateException("Kan ikke legge til saksopplysning på denne behandlingen")
    }

    fun toDTO(): BehandlingDTO {
        throw IllegalStateException("Kan ikke gjøres om til en DTO")
    }
}

data class BehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val søknad: Søknad,
    val saksopplysninger: List<Saksopplysning>,
    val vurderinger: List<Vurdering>,
)
