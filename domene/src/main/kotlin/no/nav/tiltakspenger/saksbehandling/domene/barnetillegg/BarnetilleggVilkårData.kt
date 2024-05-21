package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall

/*
TODO: Skal vi ta vare på de originale dataene om barna hvis saksbehandler korrigerer noe? Ja, det vil jeg tro
TODO: Hvorfor kalle avklarFakta og vilkårsvurder hele tiden? Når data settes, så burde det gå automatisk.
      Tilstanden til objektet burde alltid være ferdig vilkårsvurdert, det er ikke noe problem iom at vi har
      KREVER_MANUELL_VURDERING
 */
data class BarnetilleggVilkårData private constructor(
    val vurderingsperiode: Periode,
    val barnetilleggBarn: List<BarnetilleggBarn>,
    val samletVurdering: Periodisering<UtfallForBarnetilleggPeriode>,
    val antallBarnPeriodisering: Periodisering<Int>,
) {

    fun oppdaterVurderingsperiode(nyVurderingsperiode: Periode): BarnetilleggVilkårData {
        require(nyVurderingsperiode.inneholderHele(this.vurderingsperiode)) { "Vurderingsperioden kan kun utvides" }

        return this.copy(
            vurderingsperiode = nyVurderingsperiode,
            barnetilleggBarn = barnetilleggBarn.map { it.oppdaterVurderingsperiode(nyVurderingsperiode) },
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiSaksopplysningCommand): BarnetilleggVilkårData {
        require(barnetilleggBarn.find { it.id == oppdatering.barn } != null) { "Barnet finnes ikke" }

        return this.copy(
            barnetilleggBarn = this.barnetilleggBarn.map {
                if (it.id == oppdatering.barn) it.oppdaterSaksopplysning(oppdatering) else it
            },
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiPeriodeSaksopplysningCommand): BarnetilleggVilkårData {
        if (barnetilleggBarn.find { it.id == oppdatering.barn } == null) throw IllegalStateException("Barnet finnes ikke")

        return this.copy(
            barnetilleggBarn = this.barnetilleggBarn.map {
                if (it.id == oppdatering.barn) it.oppdaterSaksopplysning(oppdatering) else it
            },
        ).vilkårsvurder()
    }

    fun oppdaterSøknad(søknad: Søknad, systembruker: Systembruker): BarnetilleggVilkårData {
        // Når en ny søknad mottas nukes alle eksisterende data, inkl det saksbehandler har lagt inn.
        // TODO: Dette er trolig ikke bra nok. Hvis det er like barn i de to søknadene kan de vel beholdes f.eks?

        return this.copy(
            barnetilleggBarn = SøknadBarnetilleggMapper.map(søknad, vurderingsperiode, systembruker),
        ).vilkårsvurder()
    }

    private fun vilkårsvurder(): BarnetilleggVilkårData {
        return this.copy(
            samletVurdering = samletVurdering(vurderingsperiode, barnetilleggBarn),
            antallBarnPeriodisering = antallBarnPeriodisering(vurderingsperiode, barnetilleggBarn),
        )
    }

    fun antallBarn(): Int = antallBarnPeriodisering.maksAntall()

    companion object {
        operator fun invoke(vurderingsperiode: Periode): BarnetilleggVilkårData {
            val barnetilleggBarn = emptyList<BarnetilleggBarn>()
            return BarnetilleggVilkårData(
                vurderingsperiode = vurderingsperiode,
                barnetilleggBarn = barnetilleggBarn,
                samletVurdering = samletVurdering(vurderingsperiode, barnetilleggBarn),
                antallBarnPeriodisering = antallBarnPeriodisering(vurderingsperiode, barnetilleggBarn),
            )
        }

        private fun samletVurdering(
            vurderingsperiode: Periode,
            barnetilleggBarn: List<BarnetilleggBarn>,
        ): Periodisering<UtfallForBarnetilleggPeriode> =
            kombinerVurderinger(vurderingsperiode, barnetilleggBarn)

        private fun antallBarnPeriodisering(
            vurderingsperiode: Periode,
            barnetilleggBarn: List<BarnetilleggBarn>,
        ): Periodisering<Int> =
            kombinerAntallbarn(vurderingsperiode, barnetilleggBarn)

        private fun kombinerVurderinger(
            vurderingsperiode: Periode,
            barnetilleggBarn: List<BarnetilleggBarn>,
        ): Periodisering<UtfallForBarnetilleggPeriode> {
            if (barnetilleggBarn.isEmpty()) {
                return Periodisering(UtfallForBarnetilleggPeriode.KREVER_MANUELL_VURDERING, vurderingsperiode)
            }
            return barnetilleggBarn.map { it.samletVurdering }
                .reduser { utfall1, utfall2 ->
                    when {
                        utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                        utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                        else -> Utfall.OPPFYLT
                    }
                }.map { utfall ->
                    when (utfall) {
                        Utfall.OPPFYLT -> UtfallForBarnetilleggPeriode.GIR_RETT_BARNETILLEGG
                        Utfall.IKKE_OPPFYLT -> UtfallForBarnetilleggPeriode.GIR_IKKE_RETT_BARNETILLEGG
                        Utfall.KREVER_MANUELL_VURDERING -> UtfallForBarnetilleggPeriode.KREVER_MANUELL_VURDERING
                    }
                }
        }

        private fun kombinerAntallbarn(
            vurderingsperiode: Periode,
            barnetilleggBarn: List<BarnetilleggBarn>,
        ): Periodisering<Int> {
            if (barnetilleggBarn.isEmpty()) {
                return Periodisering(0, vurderingsperiode)
            }
            return barnetilleggBarn.map { it.samletVurdering }
                .map {
                    it.map { utfall ->
                        when (utfall) {
                            Utfall.OPPFYLT -> 1
                            Utfall.IKKE_OPPFYLT -> 0
                            Utfall.KREVER_MANUELL_VURDERING -> 0
                        }
                    }
                }.reduser { antall1, antall2 ->
                    antall1 + antall2
                }
        }

        private fun Periodisering<Int>.maksAntall(): Int = this.perioder().maxOfOrNull { it.verdi } ?: 0
    }
}
