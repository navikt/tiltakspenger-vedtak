package no.nav.tiltakspenger.vedtak.routes.behandling

data class SaksopplysningDTO(
    val fra: String,
    val til: String,
    val vilkår: String,
    val begrunnelse: String,
    val harYtelse: Boolean,
)
