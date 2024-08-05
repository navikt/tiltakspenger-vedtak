package no.nav.tiltakspenger.saksbehandling.domene.vilkår

/**
 * Det samlede utfallet for et vilkår og vilkårsettet.
 * Forskjellen fra [SamletUtfall] er at vi ikke har mulighet til å være UAVKLART.
 */
enum class AvklartSamletUtfall {
    OPPFYLT,
    DELVIS_OPPFYLT,
    IKKE_OPPFYLT,
}
