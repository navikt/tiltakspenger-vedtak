package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall

/**
 * Det samlede utfallet for et vilkår.
 */
internal enum class SamletUtfallDTO {
    OPPFYLT,
    DELVIS_OPPFYLT,
    IKKE_OPPFYLT,
    UAVKLART,
}

internal fun SamletUtfall.toDTO(): SamletUtfallDTO =
    when (this) {
        SamletUtfall.OPPFYLT -> SamletUtfallDTO.OPPFYLT
        SamletUtfall.DELVIS_OPPFYLT -> SamletUtfallDTO.DELVIS_OPPFYLT
        SamletUtfall.IKKE_OPPFYLT -> SamletUtfallDTO.IKKE_OPPFYLT
        SamletUtfall.UAVKLART -> SamletUtfallDTO.UAVKLART
    }
