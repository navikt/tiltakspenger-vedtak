package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett

// TODO pre-revurdering jah: Må støtte flere enn 1 vedtak.
fun Sak.vilkårssettTidslinje(): Vilkårssett? {
    if (rammevedtak == null) return null
    return rammevedtak.behandling.vilkårssett
}

// TODO pre-revurdering jah: Må støtte flere enn 1 vedtak.
fun Sak.stønadsdagerTidslinje(): Stønadsdager? {
    if (rammevedtak == null) return null
    return rammevedtak.behandling.stønadsdager
}

// TODO pre-revurdering jah: Må støtte flere enn 1 vedtak.
// TODO pre-to-tiltak jah: Lag en PeriodiseringMedHull type. Denne kan brukes til å representere tidslinjer med hull, som vil være tilfelle ved flere ikke-overlappende tiltaksdeltagelser.
fun Sak.utfallsperioder(): Periodisering<AvklartUtfallForPeriode>? {
    return rammevedtak?.utfallsperioder
}
