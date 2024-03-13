package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.behandling.Barnetillegg
import no.nav.tiltakspenger.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.domene.behandling.Utfallsperiode

fun BehandlingOpprettet.vilkårsvurder(): BehandlingVilkårsvurdert {
    val vurderinger = saksopplysninger().flatMap {
        it.lagVurdering(vurderingsperiode)
    }

    val utfallsperioder =
        vurderingsperiode.fra.datesUntil(vurderingsperiode.til.plusDays(1)).toList().map { dag ->
            val idag = vurderinger.filter { dag >= it.fom && dag <= it.tom }
            val utfallYtelser = when {
                idag.any { it.utfall == Utfall.KREVER_MANUELL_VURDERING } -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                idag.all { it.utfall == Utfall.OPPFYLT } -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                else -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
            }

            val harManuelleBarnUnder16 = this.søknad().barnetillegg.filterIsInstance<Barnetillegg.Manuell>()
                .filter { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja }.count { it.under16ForDato(dag) } > 0

            val utfall =
                if (utfallYtelser == UtfallForPeriode.GIR_RETT_TILTAKSPENGER && harManuelleBarnUnder16) UtfallForPeriode.KREVER_MANUELL_VURDERING else utfallYtelser

            Utfallsperiode(
                fom = dag,
                tom = dag,
                antallBarn = this.søknad().barnetillegg.filter { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja }
                    .count { it.under16ForDato(dag) },
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
        saksopplysninger = saksopplysninger,
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
