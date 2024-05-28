package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.RevurderingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.RevurderingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsdetaljer
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelserVilkårData.Companion.kombinerToUtfall

fun RevurderingOpprettet.vilkårsvurder(): RevurderingVilkårsvurdert {
    TODO()
}

fun BehandlingOpprettet.vilkårsvurder(): BehandlingVilkårsvurdert {
    val tiltaksdeltakelseUtfall: Periodisering<Utfall> =
        tiltak.map { tiltak -> tiltak.vilkårsvurderTiltaksdeltagelse() }
            .samletUtfall()

    val samletUtfall: Periodisering<UtfallForPeriode> = this.ytelserVilkårData.samletUtfall()
        .kombiner(tiltaksdeltakelseUtfall, ::kombinerToUtfall)
        .map {
            when (it) {
                Utfall.KREVER_MANUELL_VURDERING -> UtfallForPeriode.KREVER_MANUELL_VURDERING
                Utfall.IKKE_OPPFYLT -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                Utfall.OPPFYLT -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
            }
        }

    val utfallsperioder: Periodisering<Utfallsdetaljer> =
        samletUtfall.map { Utfallsdetaljer(1, it) }

    val status = if (utfallsperioder.perioder().any { it.verdi.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING }) {
        BehandlingStatus.Manuell
    } else if (utfallsperioder.perioder().any { it.verdi.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
        BehandlingStatus.Innvilget
    } else {
        BehandlingStatus.Avslag
    }
    return BehandlingVilkårsvurdert(
        id = id,
        sakId = sakId,
        søknader = søknader,
        vurderingsperiode = vurderingsperiode,
        ytelserVilkårData = ytelserVilkårData,
        tiltak = tiltak,
        saksbehandler = saksbehandler,
        utfallsperioder = utfallsperioder,
        status = status,
    )
}

private fun List<Vurdering>.samletUtfall(): Periodisering<Utfall> {
    return TODO()
}
