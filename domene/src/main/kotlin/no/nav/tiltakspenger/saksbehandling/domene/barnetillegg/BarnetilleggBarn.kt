package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.BarnetilleggBarnId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class BarnetilleggBarn(
    val id: BarnetilleggBarnId = BarnetilleggBarnId.random(),
    val detaljer: KorrigerbareBarnDetaljer,
    val harSøktBarnetilleggForDetteBarnet: KorrigerbartJaNeiVilkår,
    val forsørgesAvSøker: KorrigerbartJaNeiPeriodeVilkår,
    val bosattIEØS: KorrigerbartJaNeiPeriodeVilkår,
    val oppholderSegIEØS: KorrigerbartJaNeiPeriodeVilkår,
    val samletVurdering: Periodisering<Utfall>,
) {
    fun oppdaterVurderingsperiode(nyVurderingsperiode: Periode): BarnetilleggBarn {
        val nyForsørgesAvSøker = forsørgesAvSøker.oppdaterVurderingsperiode(nyVurderingsperiode)
        val nyBosattIEØS = bosattIEØS.oppdaterVurderingsperiode(nyVurderingsperiode)
        val nyOppholderSegIEØS = oppholderSegIEØS.oppdaterVurderingsperiode(nyVurderingsperiode)
        return this.copy(
            forsørgesAvSøker = nyForsørgesAvSøker,
            bosattIEØS = nyBosattIEØS,
            oppholderSegIEØS = nyOppholderSegIEØS,
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiSaksopplysningCommand): BarnetilleggBarn {
        require(oppdatering.barn == this.id) { "Feil barn" }
        return this.copy(
            harSøktBarnetilleggForDetteBarnet =
            harSøktBarnetilleggForDetteBarnet.oppdaterSaksopplysning(oppdatering),
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiPeriodeSaksopplysningCommand): BarnetilleggBarn {
        require(oppdatering.barn == this.id) { "Feil barn" }
        // TODO: Lage korrekte vilkår
        return when (oppdatering.vilkår) {
            Vilkår.AAP -> {
                this.copy(
                    forsørgesAvSøker = forsørgesAvSøker.oppdaterSaksopplysning(oppdatering),
                )
            }

            Vilkår.DAGPENGER -> {
                this.copy(
                    bosattIEØS = bosattIEØS.oppdaterSaksopplysning(oppdatering),
                )
            }

            Vilkår.ALDER -> {
                this.copy(
                    oppholderSegIEØS = oppholderSegIEØS.oppdaterSaksopplysning(oppdatering),
                )
            }

            else -> {
                throw IllegalArgumentException("Ugyldig vilkår")
            }
        }.vilkårsvurder()
    }

    private fun vilkårsvurder(): BarnetilleggBarn {
        return this.copy(
            samletVurdering = vilkårsvurder(
                harSøktBarnetilleggForDetteBarnet,
                forsørgesAvSøker,
                bosattIEØS,
                oppholderSegIEØS,
            ),
        )
    }

    companion object {

        // TODO: Kan de tre siste vilkårene fylles ut med default-verdier, så de ikke trenger å være parametre?
        operator fun invoke(
            detaljer: KorrigerbareBarnDetaljer,
            harSøktBarnetilleggForDetteBarnet: KorrigerbartJaNeiVilkår,
            forsørgesAvSøker: KorrigerbartJaNeiPeriodeVilkår,
            bosattIEØS: KorrigerbartJaNeiPeriodeVilkår,
            oppholderSegIEØS: KorrigerbartJaNeiPeriodeVilkår,
        ): BarnetilleggBarn =
            BarnetilleggBarn(
                detaljer,
                harSøktBarnetilleggForDetteBarnet,
                forsørgesAvSøker,
                bosattIEØS,
                oppholderSegIEØS,
            )

        private fun vilkårsvurder(
            harSøktBarnetilleggForDetteBarnet: KorrigerbartJaNeiVilkår,
            forsørgesAvSøker: KorrigerbartJaNeiPeriodeVilkår,
            bosattIEØS: KorrigerbartJaNeiPeriodeVilkår,
            oppholderSegIEØS: KorrigerbartJaNeiPeriodeVilkår,
        ): Periodisering<Utfall> = TODO("Må finne skjæringspunkter og greier")
    }
}
