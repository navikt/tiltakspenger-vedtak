package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

/**
 * Tiltak
 *
 * @param registerSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class TiltakVilkår private constructor(
    val registerSaksopplysning: TiltakSaksopplysning,
    val saksbehandlerSaksopplysning: TiltakSaksopplysning?,
    val avklartSaksopplysning: TiltakSaksopplysning,
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

    override val lovreferanse = Lovreferanse.TILTAKSDELTAGELSE

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilTiltakSaksopplysningCommand): TiltakVilkår {
        val tiltakSaksopplysning = TiltakSaksopplysning.Saksbehandler(
            tiltak = command.tiltak,
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = tiltakSaksopplysning,
            avklartSaksopplysning = tiltakSaksopplysning,
            utfall = tiltakSaksopplysning.vurderMaskinelt(),
        )
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: TiltakSaksopplysning,
            vurderingsperiode: Periode,
        ): TiltakVilkår {
            return TiltakVilkår(
                registerSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
                vurderingsperiode = vurderingsperiode,
                utfall = søknadSaksopplysning.vurderMaskinelt(),
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: TiltakSaksopplysning,
            saksbehandlerSaksopplysning: TiltakSaksopplysning?,
            avklartSaksopplysning: TiltakSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<Utfall2>,
        ): TiltakVilkår {
            return TiltakVilkår(
                registerSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
                utfall = utfall,
            )
        }
    }
}
