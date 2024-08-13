package no.nav.tiltakspenger.saksbehandling.domene.vilkår

/**
 * Utfall for en sammenhengende periode.
 * Dersom det gjelder utfall for en periodisering, skal [SamletUtfall] brukes.
 * Forskjellgen fra [UtfallForPeriode] er at vi ikke har mulighet til å være UAVKLART.
 */
enum class AvklartUtfallForPeriode {
    OPPFYLT,
    IKKE_OPPFYLT,
}
