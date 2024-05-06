package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class InngangsvilkårDTO(
    val tiltaksdeltagelser: List<TiltaksdeltagelseDTO>,
)

data class TiltaksdeltagelseDTO(
    val deltagelsesperioder: List<DeltagelsesperiodeDTO>,
    val tiltaksvariant: String,
    val status: String,
    val periode: Periode,
    val harSøkt: Boolean,
    val girRett: Boolean,
    val kilde: Kilde,
)

data class DeltagelsesperiodeDTO(
    val periode: Periode,
    val antallDager: Int,
    val deltagelse: String,
)
