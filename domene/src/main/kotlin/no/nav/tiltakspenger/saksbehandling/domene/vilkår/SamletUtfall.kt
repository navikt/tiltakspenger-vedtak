package no.nav.tiltakspenger.saksbehandling.domene.vilkår

/**
 * Det samlede utfallet for et vilkår og vilkårsettet.
 * Forskjellen fra [Utfall2] er at her kan vi og ha [SamletUtfall.DELVIS_OPPFYLT].
 */
enum class SamletUtfall {
    OPPFYLT,
    DELVIS_OPPFYLT,
    IKKE_OPPFYLT,
    UAVKLART,
}
