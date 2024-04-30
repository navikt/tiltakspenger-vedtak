package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class InngangsvilkårDTO(
    val tiltakdeltakelser: List<TiltakDeltakelseDTO>,
)

data class TiltakDeltakelseDTO(
    val deltakelser: List<DeltakelseDTO>,
    val tiltaksvariant: String,
    val status: String,
    val tiltaksPeriode: Periode,
    val harSøkt: Boolean,
    val girRett: Boolean,
    val kilde: Kilde,
)

data class DeltakelseDTO(
    val periode: Periode,
    val antallDager: Int,
    val deltakelse: String,
)
