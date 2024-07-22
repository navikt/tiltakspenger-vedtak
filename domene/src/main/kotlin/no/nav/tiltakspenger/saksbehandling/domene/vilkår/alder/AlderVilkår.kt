package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

/**
 * Alder
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class AlderVilkår private constructor(
    val søknadSaksopplysning: AlderSaksopplysning,
    val saksbehandlerSaksopplysning: AlderSaksopplysning?,
    val avklartSaksopplysning: AlderSaksopplysning,
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

    override val lovreferanse = Lovreferanse.ALDER

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
            utfall = introSaksopplysning.vurderMaskinelt(vurderingsperiode),
        )
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: AlderSaksopplysning,
            vurderingsperiode: Periode,
        ): AlderVilkår {
            return AlderVilkår(
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
            søknadSaksopplysning: AlderSaksopplysning,
            saksbehandlerSaksopplysning: AlderSaksopplysning?,
            avklartSaksopplysning: AlderSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<Utfall2>,
        ): AlderVilkår {
            return AlderVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
                utfall = utfall,
            )
        }
    }
}
