package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

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
            samletVurdering = samletVurdering(barnetilleggBarn),
            antallBarnPeriodisering = antallBarnPeriodisering(barnetilleggBarn),
        )
    }

    fun antallBarn(): Int = antallBarnPeriodisering.maksAntall()

    companion object {
        operator fun invoke(vurderingsperiode: Periode): BarnetilleggVilkårData {
            val barnetilleggBarn = emptyList<BarnetilleggBarn>()
            return BarnetilleggVilkårData(
                vurderingsperiode,
                barnetilleggBarn,
                samletVurdering(barnetilleggBarn),
                antallBarnPeriodisering(barnetilleggBarn),
            )
        }

        private fun samletVurdering(barnetilleggBarn: List<BarnetilleggBarn>): Periodisering<UtfallForBarnetilleggPeriode> =
            barnetilleggBarn.kombinerVurderinger()

        private fun antallBarnPeriodisering(barnetilleggBarn: List<BarnetilleggBarn>): Periodisering<Int> =
            barnetilleggBarn.kombinerAntallbarn()

        private fun List<BarnetilleggBarn>.kombinerVurderinger(): Periodisering<UtfallForBarnetilleggPeriode> =
            TODO("Må finne skjæringspunktene")

        private fun List<BarnetilleggBarn>.kombinerAntallbarn(): Periodisering<Int> =
            TODO("Må finne skjæringspunktene")

        private fun Periodisering<Int>.maksAntall(): Int = this.periodeMedVerdi.maxOfOrNull { it.value } ?: 0
    }
}
