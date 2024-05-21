package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.BarnetilleggBarnId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
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

    fun oppdaterSaksopplysning(command: OppdaterJaNeiSaksopplysningCommand): BarnetilleggBarn {
        require(command.barn == this.id) { "Feil barn" }
        if (command.vilkår == Vilkår.BARNETILLEGG_SØKT) {
            return this.copy(
                harSøktBarnetilleggForDetteBarnet =
                harSøktBarnetilleggForDetteBarnet.oppdaterSaksopplysning(command),
            ).vilkårsvurder()
        } else {
            throw IllegalArgumentException("Ukjent vilkår ${command.vilkår} for oppdatering av barnetillegg")
        }
    }

    fun oppdaterSaksopplysning(command: OppdaterJaNeiPeriodeSaksopplysningCommand): BarnetilleggBarn {
        require(command.barn == this.id) { "Feil barn" }
        return when (command.vilkår) {
            Vilkår.BARNETILLEGG_FORSØRGES -> {
                this.copy(
                    forsørgesAvSøker = forsørgesAvSøker.oppdaterSaksopplysning(command),
                )
            }

            Vilkår.BARNETILLEGG_BOSATT -> {
                this.copy(
                    bosattIEØS = bosattIEØS.oppdaterSaksopplysning(command),
                )
            }

            Vilkår.BARNETILLEGG_OPPHOLD -> {
                this.copy(
                    oppholderSegIEØS = oppholderSegIEØS.oppdaterSaksopplysning(command),
                )
            }

            else -> {
                throw IllegalArgumentException("Ukjent vilkår ${command.vilkår} for oppdatering av barnetillegg")
            }
        }.vilkårsvurder()
    }

    private fun vilkårsvurder(): BarnetilleggBarn {
        return this.copy(
            samletVurdering = vilkårsvurder(
                harSøktBarnetilleggForDetteBarnet = harSøktBarnetilleggForDetteBarnet,
                forsørgesAvSøker = forsørgesAvSøker,
                bosattIEØS = bosattIEØS,
                oppholderSegIEØS = oppholderSegIEØS,
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
                detaljer = detaljer,
                harSøktBarnetilleggForDetteBarnet = harSøktBarnetilleggForDetteBarnet,
                forsørgesAvSøker = forsørgesAvSøker,
                bosattIEØS = bosattIEØS,
                oppholderSegIEØS = oppholderSegIEØS,
                samletVurdering = vilkårsvurder(
                    harSøktBarnetilleggForDetteBarnet = harSøktBarnetilleggForDetteBarnet,
                    forsørgesAvSøker = forsørgesAvSøker,
                    bosattIEØS = bosattIEØS,
                    oppholderSegIEØS = oppholderSegIEØS,
                ),
            )

        private fun vilkårsvurder(
            harSøktBarnetilleggForDetteBarnet: KorrigerbartJaNeiVilkår,
            forsørgesAvSøker: KorrigerbartJaNeiPeriodeVilkår,
            bosattIEØS: KorrigerbartJaNeiPeriodeVilkår,
            oppholderSegIEØS: KorrigerbartJaNeiPeriodeVilkår,
        ): Periodisering<Utfall> {
            return listOf(
                harSøktBarnetilleggForDetteBarnet.vurdering,
                forsørgesAvSøker.vurdering,
                bosattIEØS.vurdering,
                oppholderSegIEØS.vurdering,
            ).reduser { utfall1, utfall2 ->
                when {
                    utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                    utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                    else -> Utfall.OPPFYLT
                }
            }
        }
    }
}
