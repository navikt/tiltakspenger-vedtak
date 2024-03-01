package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.lagVurdering

fun Søknadsbehandling.Opprettet.vilkårsvurder(): BehandlingVilkårsvurdert {
    val vurderinger = saksopplysninger().flatMap {
        it.lagVurdering(vurderingsperiode)
    }

    val utfallsperioder =
        vurderingsperiode.fra.datesUntil(vurderingsperiode.til.plusDays(1)).toList().map { dag ->
            val idag = vurderinger.filter { dag >= it.fom && dag <= it.tom }
            val utfall = when {
                idag.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING } -> BehandlingVilkårsvurdert.UtfallForPeriode.KREVER_MANUELL_VURDERING
                idag.all { it.utfall == Utfall.OPPFYLT } -> BehandlingVilkårsvurdert.UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                else -> BehandlingVilkårsvurdert.UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
            }

            BehandlingVilkårsvurdert.Utfallsperiode(
                fom = dag,
                tom = dag,
                antallBarn = 0,
                tiltak = listOf(),
                antDagerMedTiltak = 0,
                utfall = utfall,
            )
        }.fold(emptyList<BehandlingVilkårsvurdert.Utfallsperiode>()) { periodisertliste, nesteDag ->
            periodisertliste.slåSammen(nesteDag)
        }

    if (vurderinger.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING }) {
        return BehandlingVilkårsvurdert.Manuell(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            vilkårsvurderinger = vurderinger,
            saksbehandler = saksbehandler,
            utfallsperioder = utfallsperioder,
        )
    }
    if (utfallsperioder.any { it.utfall == BehandlingVilkårsvurdert.UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
        return BehandlingVilkårsvurdert.Innvilget(
            id = id,
            sakId = sakId,
            søknader = søknader,
            vurderingsperiode = vurderingsperiode,
            saksopplysninger = saksopplysninger,
            tiltak = tiltak,
            vilkårsvurderinger = vurderinger,
            saksbehandler = saksbehandler,
            utfallsperioder = utfallsperioder,
        )
    }
    return BehandlingVilkårsvurdert.Avslag(
        id = id,
        sakId = sakId,
        søknader = søknader,
        vurderingsperiode = vurderingsperiode,
        saksopplysninger = saksopplysninger,
        tiltak = tiltak,
        vilkårsvurderinger = vurderinger,
        saksbehandler = saksbehandler,
        utfallsperioder = utfallsperioder,
    )
}

private fun List<BehandlingVilkårsvurdert.Utfallsperiode>.slåSammen(neste: BehandlingVilkårsvurdert.Utfallsperiode): List<BehandlingVilkårsvurdert.Utfallsperiode> {
    if (this.isEmpty()) return listOf(neste)
    val forrige = this.last()
    return if (forrige == neste) {
        this.dropLast(1) + forrige.copy(
            tom = neste.tom,
        )
    } else {
        this + neste
    }
}
