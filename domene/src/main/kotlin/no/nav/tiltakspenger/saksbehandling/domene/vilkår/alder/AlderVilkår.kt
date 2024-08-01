package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

/**
 * Alder
 *
 * @param registerSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class AlderVilkår private constructor(
    val registerSaksopplysning: AlderSaksopplysning,
    val saksbehandlerSaksopplysning: AlderSaksopplysning?,
    val avklartSaksopplysning: AlderSaksopplysning,
    val vurderingsperiode: Periode,
) : SkalErstatteVilkår {

    override val lovreferanse = Lovreferanse.ALDER

    override fun utfall(): Periodisering<Utfall2> {
        // Om noen har bursdag 29. mars (skuddår) og de akuratt har fylt 18 vil fødselsdagen bli satt til 28. mars, og de vil få krav på tiltakspenger én dag før de er 18.
        // Dette er så cornercase at vi per nå ikke bruker tid på å skrive en egen håndtering av 'plusYears()' for å støtte dette caset.
        val dagenBrukerFyller18År = avklartSaksopplysning.fødselsdato.plusYears(18)
        return when {
            dagenBrukerFyller18År.isBefore(vurderingsperiode.fraOgMed) -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
            dagenBrukerFyller18År.isAfter(vurderingsperiode.tilOgMed) -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
            else -> {
                Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
            }
        }
    }

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilAlderSaksopplysningCommand): AlderVilkår {
        val introSaksopplysning = AlderSaksopplysning.Saksbehandler(
            fødselsdato = command.fødselsdato,
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = introSaksopplysning,
            avklartSaksopplysning = introSaksopplysning,
        )
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: AlderSaksopplysning,
            vurderingsperiode: Periode,
        ): AlderVilkår {
            return AlderVilkår(
                registerSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: AlderSaksopplysning,
            saksbehandlerSaksopplysning: AlderSaksopplysning?,
            avklartSaksopplysning: AlderSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<Utfall2>,
        ): AlderVilkår {
            return AlderVilkår(
                registerSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
