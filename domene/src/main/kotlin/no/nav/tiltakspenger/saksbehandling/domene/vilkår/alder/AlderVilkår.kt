package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.StøtterIkkeUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDateTime

/**
 * Alder
 *
 * @param registerSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 */
data class AlderVilkår private constructor(
    override val vurderingsperiode: Periode,
    override val utfall: Periodisering<UtfallForPeriode>,
    val registerSaksopplysning: AlderSaksopplysning.Register,
    val saksbehandlerSaksopplysning: AlderSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: AlderSaksopplysning,
) : Vilkår {
    override val lovreferanse = Lovreferanse.ALDER

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilAlderSaksopplysningCommand): AlderVilkår {
        val introSaksopplysning =
            AlderSaksopplysning.Saksbehandler(
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
            registerSaksopplysning: AlderSaksopplysning.Register,
            vurderingsperiode: Periode,
        ): Either<StøtterIkkeUtfall, AlderVilkår> {
            val utfall: Periodisering<UtfallForPeriode> =
                run {
                    // Om noen har bursdag 29. mars (skuddår) og de akkurat har fylt 18 vil fødselsdagen bli satt til 28. mars, og de vil få krav på tiltakspenger én dag før de er 18.
                    // Dette er så cornercase at vi per nå velger ikke å gjøre det pga. a) veldig lav forekomst/sannsynlighet og b) konsekvens; dette er i brukers favør.
                    val dagenBrukerFyller18År = registerSaksopplysning.fødselsdato.plusYears(18)
                    when {
                        dagenBrukerFyller18År.isAfter(vurderingsperiode.fraOgMed) -> return StøtterIkkeUtfall.DelvisInnvilgelse.left()
                        else -> {
                            Periodisering(
                                UtfallForPeriode.OPPFYLT,
                                vurderingsperiode,
                            )
                        }
                    }
                }

            return AlderVilkår(
                registerSaksopplysning = registerSaksopplysning,
                saksbehandlerSaksopplysning = null,
                utfall = utfall,
                avklartSaksopplysning = registerSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).right()
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            registerSaksopplysning: AlderSaksopplysning.Register,
            saksbehandlerSaksopplysning: AlderSaksopplysning.Saksbehandler?,
            avklartSaksopplysning: AlderSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<UtfallForPeriode>,
        ): AlderVilkår =
            AlderVilkår(
                registerSaksopplysning = registerSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
                utfall = utfall,
            ).also {
                check(utfall == it.utfall) {
                    "Mismatch mellom utfallet som er lagret i AlderVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall})"
                }
            }
    }
}
