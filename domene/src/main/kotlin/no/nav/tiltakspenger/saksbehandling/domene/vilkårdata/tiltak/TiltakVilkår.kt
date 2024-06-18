package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.tiltak

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.Inngangsvilkårsbehandling

data class TiltakVilkår(
    val vurderingsperiode: Periode,
    val tiltak: List<Tiltak>,
    val vurdering: Vurdering,
) : Inngangsvilkårsbehandling {
    override fun vilkår(): Inngangsvilkår {
        return Inngangsvilkår.TILTAKSDELTAGELSE
    }

    override fun vurdering(): Vurdering {
        return vurdering
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): TiltakVilkår {
        return this.copy(tiltak = tiltak).vilkårsvurder()
    }

    fun oppdaterAntallDager(
        tiltakId: String,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): TiltakVilkår {
        val tiltakTilOppdatering = tiltak.find { it.id.toString() == tiltakId }
        check(tiltakTilOppdatering != null) { "Kan ikke oppdatere antall dager fordi vi fant ikke tiltaket på behandlingen" }

        val oppdatertTiltak = tiltakTilOppdatering.leggTilAntallDagerFraSaksbehandler(nyPeriodeMedAntallDager)

        val nyeTiltak = tiltak.map {
            if (it.eksternId == oppdatertTiltak.eksternId) {
                oppdatertTiltak
            } else {
                it
            }
        }

        return this.copy(tiltak = nyeTiltak).vilkårsvurder()
    }

    private fun vilkårsvurder(): TiltakVilkår {
        return this.copy(vurdering = vilkårsvurder(vurderingsperiode, tiltak))
    }

    companion object {
        operator fun invoke(vurderingsperiode: Periode): TiltakVilkår {
            return TiltakVilkår(
                vurderingsperiode,
                emptyList(),
                vilkårsvurder(vurderingsperiode, emptyList()),
            )
        }

        fun vilkårsvurder(vurderingsperiode: Periode, tiltak: List<Tiltak>): Vurdering =
            tiltak
                .map { it.vilkårsvurderTiltaksdeltagelse() }
                .map { it.utfall }
                .fold(Periodisering(Utfall.UAVKLART, vurderingsperiode)) { resultat, annen ->
                    resultat.kombiner(annen) { utfall1, utfall2 ->
                        when {
                            utfall1 == Utfall.OPPFYLT || utfall2 == Utfall.OPPFYLT -> Utfall.OPPFYLT
                            utfall1 == Utfall.UAVKLART || utfall2 == Utfall.UAVKLART -> Utfall.UAVKLART
                            else -> Utfall.IKKE_OPPFYLT
                        }
                    }
                }.let {
                    Vurdering(
                        utfall = it,
                        detaljer = "",
                    )
                }
    }
}
