package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Barnetillegg
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.RevurderingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.RevurderingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.BarnSaksopplysning

fun RevurderingOpprettet.vilkårsvurder(): RevurderingVilkårsvurdert {
    TODO()
}

fun BehandlingOpprettet.vilkårsvurder(): BehandlingVilkårsvurdert {
    val (vurderingerBarn, vurderingerResten) = this.vilkårData.map {
        it.vilkårsvurder().vurderinger
    }.flatten().partition { it.vilkår == Vilkår.BARNETILLEGG }

    val utfallsperioder =
        vurderingsperiode.fra.datesUntil(vurderingsperiode.til.plusDays(1)).toList().map { dag ->
            val idag = vurderingerResten.filter { dag >= it.fom && dag <= it.tom }

            val antallBarnIDag = vurderingerBarn
                .filter { it.utfall == Utfall.OPPFYLT }
                .filter { dag >= it.fom && dag <= it.tom }.size

            val utfallRettTilTiltakspenger = when {
                idag.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING } -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                idag.all { it.utfall == Utfall.OPPFYLT } -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                else -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
            }
            val utfall = if (utfallRettTilTiltakspenger == UtfallForPeriode.GIR_RETT_TILTAKSPENGER)
                UtfallForPeriode.KREVER_MANUELL_VURDERING
            else
                utfallRettTilTiltakspenger

            Utfallsperiode(
                fom = dag,
                tom = dag,
                antallBarn = antallBarnIDag,
                utfall = utfall,
            )
        }.fold(emptyList<Utfallsperiode>()) { periodisertliste, nesteDag ->
            periodisertliste.slåSammen(nesteDag)
        }

    val status = if (utfallsperioder.any { it.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
        BehandlingStatus.Manuell
    } else if (utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
        BehandlingStatus.Innvilget
    } else {
        BehandlingStatus.Avslag
    }
    return BehandlingVilkårsvurdert(
        id = id,
        sakId = sakId,
        søknader = søknader,
        vurderingsperiode = vurderingsperiode,
        saksopplysninger = avklarteSaksopplysninger,
        tiltak = tiltak,
        vilkårsvurderinger = vurderinger,
        saksbehandler = saksbehandler,
        utfallsperioder = utfallsperioder,
        status = status,
    )
}

private fun List<Utfallsperiode>.slåSammen(neste: Utfallsperiode): List<Utfallsperiode> {
    if (this.isEmpty()) return listOf(neste)
    val forrige = this.last()
    return if (forrige.kanSlåsSammen(neste)) {
        this.dropLast(1) + forrige.copy(
            tom = neste.tom,
        )
    } else {
        this + neste
    }
}
