package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import java.time.LocalDateTime

/**
 * Livsoppholdytelser skal ha minimal med støtte ved lansering for én bruker.
 *
 *
 * @param harEnEllerFlereYtelserFraSøknaden Sier noe om det ble oppgitt.
 * @param saksbehandlerUtfall Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 *
 */
data class LivsoppholdVilkår private constructor(
    val harEnEllerFlereYtelserFraSøknaden: Boolean,
    val saksbehandlerUtfall: Utfall2,
    val periode: Periode,
) : SkalErstatteVilkår {

    val samletUtfall: SamletUtfall = when {
        harEnEllerFlereYtelserFraSøknaden -> SamletUtfall.IKKE_OPPFYLT
        saksbehandlerUtfall == Utfall2.IKKE_OPPFYLT -> SamletUtfall.IKKE_OPPFYLT
        saksbehandlerUtfall == Utfall2.OPPFYLT -> SamletUtfall.OPPFYLT
        else -> throw IllegalStateException("Ugyldig utfall")
    }

    override val lovreferanse = Lovreferanse.LIVSOPPHOLDYTELSER

    // TODO kew: Dette er bare frem til vi splitter livsoppholdytelser til å inneholde alle delvilkårene.
    val totalePeriode: Periode = this.periode

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): LivsoppholdVilkår {
        val livsoppholdSaksopplysning = LivsoppholdSaksopplysning.Saksbehandler(
            deltar = Periodisering(
                command.deltakelseForPeriode.map { PeriodeMedVerdi(it.tilDeltagelse(), it.periode) },
            ).utvid(Deltagelse.DELTAR_IKKE, totalePeriode),
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerUtfall = saksbehandlerUtfall,
        )
    }

// TODO kew: her mangler det en init. Men den er det mer bruk for senere, når dette vilkåret får mer kjøtt på beina

    companion object {
        fun opprett(
            harEnEllerFlereYtelserFraSøknaden: Boolean,
            periode: Periode,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                harEnEllerFlereYtelserFraSøknaden = harEnEllerFlereYtelserFraSøknaden,
                saksbehandlerUtfall = Utfall2.UAVKLART,
                periode = periode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            harEnEllerFlereYtelserFraSøknaden: Boolean,
            saksbehandlerUtfall: Utfall2,
            periode: Periode,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                harEnEllerFlereYtelserFraSøknaden = harEnEllerFlereYtelserFraSøknaden,
                saksbehandlerUtfall = saksbehandlerUtfall,
                periode = periode

            )
        }
    }
}
