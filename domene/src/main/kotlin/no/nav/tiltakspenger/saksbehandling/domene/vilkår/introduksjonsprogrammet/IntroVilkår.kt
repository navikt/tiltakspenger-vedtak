package no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import java.time.LocalDateTime

/**
 * Introduksjonsprogrammet: https://www.regjeringen.no/no/tema/innvandring-og-integrering/asd/Verkemiddel-i-integreringsarbeidet/introduksjonsprogram/id2343472/
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class IntroVilkår private constructor(
    val søknadSaksopplysning: IntroSaksopplysning,
    val saksbehandlerSaksopplysning: IntroSaksopplysning?,
    val avklartSaksopplysning: IntroSaksopplysning,
) : SkalErstatteVilkår {

    override val lovreferanse = Lovreferanse.INTROPROGRAMMET

    override fun utfall(): Periodisering<Utfall2> {
        return avklartSaksopplysning.deltar.map { it.vurderMaskinelt() }
    }

    val totalePeriode: Periode = avklartSaksopplysning.totalePeriode

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilIntroSaksopplysningCommand): IntroVilkår {
        val introSaksopplysning = IntroSaksopplysning.Saksbehandler(
            deltar = Periodisering(
                command.deltakelseForPeriode.map { PeriodeMedVerdi(it.tilDeltagelse(), it.periode) },
            ).utvid(Deltagelse.DELTAR_IKKE, totalePeriode),
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = introSaksopplysning,
            avklartSaksopplysning = introSaksopplysning,
        )
    }

    init {
        if (saksbehandlerSaksopplysning != null) {
            require(søknadSaksopplysning.totalePeriode == saksbehandlerSaksopplysning.totalePeriode) {
                "søknadSaksopplysning (${søknadSaksopplysning.totalePeriode}) og saksbehandlerSaksopplysning(${saksbehandlerSaksopplysning.totalePeriode}) må ha samme totale periode."
            }
            require(saksbehandlerSaksopplysning.totalePeriode == avklartSaksopplysning.totalePeriode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) og avklartSaksopplysning(${avklartSaksopplysning.totalePeriode}) må ha samme totale periode."
            }
        }
        require(søknadSaksopplysning.totalePeriode == avklartSaksopplysning.totalePeriode) {
            "søknadSaksopplysning (${søknadSaksopplysning.totalePeriode}) og avklartSaksopplysning(${avklartSaksopplysning.totalePeriode}) må ha samme totale periode."
        }
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: IntroSaksopplysning,
        ): IntroVilkår {
            return IntroVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: IntroSaksopplysning,
            saksbehandlerSaksopplysning: IntroSaksopplysning?,
            avklartSaksopplysning: IntroSaksopplysning,
            utfall: Periodisering<Utfall2>,
        ): IntroVilkår {
            return IntroVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
