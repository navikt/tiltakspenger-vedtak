package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDateTime

/**
 * Livsoppholdytelser skal ha minimal med støtte ved lansering for én bruker.
 *
 *
 * @param søknadssaksopplysning Sier noe om bruker har svart at hen mottar livsoppholdytelser i søknaden.
 * @param saksbehandlerSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param avklartSaksopplysning Sier noe om hvilken saksopplysning som er gjeldende; subsumsjonen. For livsoppholdytelser er dette til og begynne med alltid saksbehandler.
 * @param vurderingsperiode Vurderingsperioden faktumene må si noe om.
 *
 */
data class LivsoppholdVilkår private constructor(
    override val vurderingsperiode: Periode,
    val søknadssaksopplysning: LivsoppholdSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: LivsoppholdSaksopplysning?,
) : Vilkår {
    override val lovreferanse = Lovreferanse.LIVSOPPHOLDYTELSER

    override fun utfall(): Periodisering<UtfallForPeriode> {
        return when {
            avklartSaksopplysning == null -> {
                Periodisering(
                    UtfallForPeriode.UAVKLART,
                    vurderingsperiode,
                )
            }
            !avklartSaksopplysning.harLivsoppholdYtelser -> {
                Periodisering(
                    UtfallForPeriode.OPPFYLT,
                    vurderingsperiode,
                )
            }
            avklartSaksopplysning.harLivsoppholdYtelser -> {
                Periodisering(
                    UtfallForPeriode.IKKE_OPPFYLT,
                    vurderingsperiode,
                )
            }
            else -> throw IllegalStateException("Ugyldig utfall")
        }
    }

    init {
        if (avklartSaksopplysning != null) require(avklartSaksopplysning.periode.inneholderHele(vurderingsperiode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }
        require(søknadssaksopplysning.periode.inneholderHele(vurderingsperiode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.periode.inneholderHele(vurderingsperiode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }

            require(avklartSaksopplysning == saksbehandlerSaksopplysning) { "Om vi har saksopplysning fra saksbehandler må den avklarte saksopplysningen være fra saksbehandler" }
        } else {
            require(avklartSaksopplysning == null) { "Dersom vi ikke har saksbehandlerSaksopplysning, skal avklartSaksopplysning være null" }
        }
    }

    val samletUtfall: SamletUtfall = when {
        avklartSaksopplysning == null -> SamletUtfall.UAVKLART
        avklartSaksopplysning.harLivsoppholdYtelser -> SamletUtfall.IKKE_OPPFYLT
        !avklartSaksopplysning.harLivsoppholdYtelser -> SamletUtfall.OPPFYLT
        else -> throw IllegalStateException("Livoppholdvilkår: Ugyldig utfall. harLivsoppholdYtelser: ${avklartSaksopplysning.harLivsoppholdYtelser}")
    }

    data object PeriodenMåVæreLikVurderingsperioden

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): Either<PeriodenMåVæreLikVurderingsperioden, LivsoppholdVilkår> {
        if (!vurderingsperiode.inneholderHele(command.harYtelseForPeriode.periode)) {
            return PeriodenMåVæreLikVurderingsperioden.left()
        }
        val livsoppholdSaksopplysning =
            LivsoppholdSaksopplysning.Saksbehandler(
                harLivsoppholdYtelser = command.harYtelseForPeriode.harYtelse,
                årsakTilEndring = command.årsakTilEndring,
                tidsstempel = LocalDateTime.now(),
                saksbehandler = command.saksbehandler,
                periode = vurderingsperiode,
            )
        return this.copy(
            saksbehandlerSaksopplysning = livsoppholdSaksopplysning,
            avklartSaksopplysning = livsoppholdSaksopplysning,
        ).right()
    }

    companion object {
        fun opprett(
            søknadSaksopplysning: LivsoppholdSaksopplysning.Søknad,
            vurderingsperiode: Periode,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = null,
                vurderingsperiode = vurderingsperiode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: LivsoppholdSaksopplysning.Søknad,
            saksbehandlerSaksopplysning: LivsoppholdSaksopplysning.Saksbehandler?,
            avklartSaksopplysning: LivsoppholdSaksopplysning?,
            vurderingsperiode: Periode,
            utfall: Periodisering<UtfallForPeriode>,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret i LivsoppholdVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
