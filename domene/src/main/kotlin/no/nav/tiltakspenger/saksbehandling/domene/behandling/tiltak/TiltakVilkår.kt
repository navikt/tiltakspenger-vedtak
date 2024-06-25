package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class TiltakVilkår(
    val tiltak: List<Tiltak> = emptyList(),
) {
    val lovreferanse = Lovreferanse.TILTAKSDELTAGELSE

    fun vilkårsvurder(): List<Vurdering> {
        return tiltak.map { tiltak -> tiltak.vilkårsvurderTiltaksdeltagelse() }
    }

    fun samletUtfall(vurderingsperiode: Periode): Periodisering<Utfall> {
        return tiltak
            .map { it.utfall(vurderingsperiode) }
            .fold(
                Periodisering(
                    Utfall.IKKE_OPPFYLT,
                    vurderingsperiode,
                ),
            ) { samlet: Periodisering<Utfall>, annen: Periodisering<Utfall> ->
                samlet.kombiner(annen) { utfall1, utfall2 ->
                    when {
                        utfall1 == Utfall.OPPFYLT || utfall2 == Utfall.OPPFYLT -> Utfall.OPPFYLT
                        utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                        else -> Utfall.IKKE_OPPFYLT
                    }
                }
            }
    }

    fun oppdaterTiltak(oppdaterteTiltak: List<Tiltak>): TiltakVilkår {
        return this.copy(tiltak = oppdaterteTiltak)
    }

    fun oppdaterAntallDager(
        tiltakId: TiltakId,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): TiltakVilkår {
        val tiltakTilOppdatering = tiltak.find { it.id == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke oppdatere antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.leggTilAntallDagerFraSaksbehandler(nyPeriodeMedAntallDager)

        val nyeTiltak = tiltak.map {
            if (it.id == oppdatertTiltak.id) {
                oppdatertTiltak
            } else {
                it
            }
        }
        return this.copy(tiltak = nyeTiltak)
    }

    fun tilbakestillAntallDager(
        tiltakId: TiltakId,
        saksbehandler: Saksbehandler,
    ): TiltakVilkår {
        val tiltakTilOppdatering = tiltak.find { it.id == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke tilbakestille antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.tilbakestillAntallDagerFraSaksbehandler()

        val nyeTiltak = tiltak.map {
            if (it.id == oppdatertTiltak.id) {
                oppdatertTiltak
            } else {
                it
            }
        }

        return this.copy(
            tiltak = nyeTiltak,
        )
    }
}
