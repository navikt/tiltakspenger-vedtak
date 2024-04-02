package no.nav.tiltakspenger.vedtak.routes.behandling

data class SaksopplysningDTO(
    val fom: String,
    val tom: String,
    val vilkår: String,
    val begrunnelse: String,
    val harYtelse: Boolean,
)
