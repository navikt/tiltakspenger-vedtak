package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import java.time.LocalDateTime

/**
 * Livsoppholdytelser skal ha minimal med støtte ved lansering for én bruker.
 *
 *
 * @param søknadssaksopplysning Sier noe om bruker har svart at hen mottar livsoppholdytelser i søknaden.
 * @param saksbehandlerUtfall Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 *
 */
data class LivsoppholdVilkår private constructor(
    val søknadssaksopplysning: LivsoppholdSaksopplysning,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysning?,
    val vurderingsPeriode: Periode,
) : SkalErstatteVilkår {
    override val lovreferanse = Lovreferanse.LIVSOPPHOLDYTELSER

    val samletUtfall: SamletUtfall = when {
        saksbehandlerSaksopplysning != null && !saksbehandlerSaksopplysning.harLivsoppholdYtelser -> SamletUtfall.OPPFYLT
        saksbehandlerSaksopplysning != null && saksbehandlerSaksopplysning.harLivsoppholdYtelser -> throw IkkeImplementertException("Støtter ikke utfall 'IKKE_OPPFYLT' fra saksbehandler")
        søknadssaksopplysning.harLivsoppholdYtelser -> SamletUtfall.IKKE_OPPFYLT
        søknadssaksopplysning.harLivsoppholdYtelser -> SamletUtfall.OPPFYLT
        saksbehandlerSaksopplysning == null -> SamletUtfall.UAVKLART
        else -> throw IllegalStateException("Ugyldig utfall")
    }

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): LivsoppholdVilkår {
        val deltar = when (command.deltakelseForPeriode.all { it.deltar }) {
            true -> true
            false -> false
        }
        val livsoppholdSaksopplysning =
            LivsoppholdSaksopplysning.Saksbehandler(
                harLivsoppholdYtelser = deltar,
                årsakTilEndring = command.årsakTilEndring,
                tidsstempel = LocalDateTime.now(),
                saksbehandler = command.saksbehandler,
                periode = vurderingsPeriode,
            )
        return this.copy(
            saksbehandlerSaksopplysning = livsoppholdSaksopplysning,
        )
    }

// TODO kew: her mangler det en init. Men den er det mer bruk for senere, når dette vilkåret får mer kjøtt på beina

    companion object {
        fun opprett(
            livsoppholdSaksopplysning: LivsoppholdSaksopplysning,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = livsoppholdSaksopplysning,
                saksbehandlerSaksopplysning = null,
                vurderingsPeriode = livsoppholdSaksopplysning.periode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            harEnEllerFlereYtelserFraSøknaden: LivsoppholdSaksopplysning,
            livsoppholdSaksopplysning: LivsoppholdSaksopplysning?,
            vurderingsPeriode: Periode,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = harEnEllerFlereYtelserFraSøknaden,
                saksbehandlerSaksopplysning = livsoppholdSaksopplysning,
                vurderingsPeriode = vurderingsPeriode,

            )
        }
    }
}
