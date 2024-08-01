package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

/**
 * Kravfrist
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class KravfristVilkår private constructor(
    val søknadSaksopplysning: KravfristSaksopplysning,
    val saksbehandlerSaksopplysning: KravfristSaksopplysning?,
    val avklartSaksopplysning: KravfristSaksopplysning,
    val vurderingsperiode: Periode,
) : SkalErstatteVilkår {

    override val lovreferanse = Lovreferanse.FRIST_FOR_FRAMSETTING_AV_KRAV

    override fun utfall(): Periodisering<Utfall2> {
        val datoDetKanInnvilgesFra = avklartSaksopplysning.kravdato.withDayOfMonth(1).minusMonths(3).toLocalDate()

        return when {
            datoDetKanInnvilgesFra <= vurderingsperiode.fraOgMed -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
            datoDetKanInnvilgesFra > vurderingsperiode.tilOgMed -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
            else -> throw IkkeImplementertException("Støtter ikke at kravdatoen ($datoDetKanInnvilgesFra) er midt i vurderingsperioden ($vurderingsperiode)")
        }
    }

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilKravfristSaksopplysningCommand): KravfristVilkår {
        val kravfristSaksopplysning = KravfristSaksopplysning.Saksbehandler(
            kravdato = command.kravdato,
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = kravfristSaksopplysning,
            avklartSaksopplysning = kravfristSaksopplysning,
        )
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: KravfristSaksopplysning,
            vurderingsperiode: Periode,
        ): KravfristVilkår {
            return KravfristVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: KravfristSaksopplysning,
            saksbehandlerSaksopplysning: KravfristSaksopplysning?,
            avklartSaksopplysning: KravfristSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<Utfall2>,
        ): KravfristVilkår {
            return KravfristVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret i KravfristVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
