package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett

/**
 * Kommentar jah: Utvid til å håndtere flere vedtak. Kan se su-se-bakover for eksempel på tidslinjealgoritme.
 */
fun Sak.vilkårssettTidslinje(): Vilkårssett? {
    if (rammevedtak == null) return null
    return rammevedtak.behandling.vilkårssett
}

/**
 * Kommentar jah: Utvid til å håndtere flere vedtak. Kan se su-se-bakover for eksempel på tidslinjealgoritme.
 */
fun Sak.stønadsdagerTidslinje(): Stønadsdager? {
    if (rammevedtak == null) return null
    return rammevedtak.behandling.stønadsdager
}
