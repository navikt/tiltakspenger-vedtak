package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
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
    val utfall: Periodisering<Utfall2>,
) : SkalErstatteVilkår {

    val samletUtfall: SamletUtfall = when {
        utfall.perioder().any { it.verdi == Utfall2.UAVKLART } -> SamletUtfall.UAVKLART
        utfall.perioder().all { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.OPPFYLT
        utfall.perioder().all { it.verdi == Utfall2.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
        utfall.perioder().any() { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
        else -> throw IllegalStateException("Ugyldig utfall")
    }

    override val lovreferanse = Lovreferanse.FRIST_FOR_FRAMSETTING_AV_KRAV

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilKravfristSaksopplysningCommand): KravfristVilkår {
        val introSaksopplysning = KravfristSaksopplysning.Saksbehandler(
            kravdato = command.kravdato,
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = introSaksopplysning,
            avklartSaksopplysning = introSaksopplysning,
            utfall = introSaksopplysning.vurderMaskinelt(vurderingsperiode),
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
                utfall = søknadSaksopplysning.vurderMaskinelt(vurderingsperiode),
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
                utfall = utfall,
            )
        }
    }
}
