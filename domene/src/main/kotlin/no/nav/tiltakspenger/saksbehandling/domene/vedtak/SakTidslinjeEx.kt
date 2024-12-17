package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett

fun Sak.vilkårssett(): Periodisering<Vilkårssett> {
    return vedtaksliste.vilkårssett
}

fun Sak.krympVilkårssett(nyPeriode: Periode): Periodisering<Vilkårssett> {
    return vedtaksliste.krympVilkårssett(nyPeriode)
}

fun Sak.stønadsdager(): Periodisering<Stønadsdager> {
    return vedtaksliste.stønadsdager
}

fun Sak.krympStønadsdager(nyPeriode: Periode): Periodisering<Stønadsdager> {
    return vedtaksliste.krympStønadsdager(nyPeriode)
}

fun Sak.utfallsperioder(): Periodisering<AvklartUtfallForPeriode> {
    return vedtaksliste.utfallsperioder
}

fun Sak.krympUtfallsperioder(nyPeriode: Periode): Periodisering<AvklartUtfallForPeriode> {
    return vedtaksliste.krympUtfallsperioder(nyPeriode)
}
