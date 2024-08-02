package no.nav.tiltakspenger.saksbehandling.domene.vilkår

/**
 * Det samlede utfallet for et vilkår og vilkårsettet.
 * Forskjellen fra [UtfallForPeriode] er at her kan vi og ha [AvklartSamletUtfall.DELVIS_OPPFYLT].
 */
enum class AvklartSamletUtfall {
    OPPFYLT,
    DELVIS_OPPFYLT,
    IKKE_OPPFYLT,
}
