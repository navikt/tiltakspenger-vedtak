package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

/**
 * Livsoppholdytelser skal ha minimal med støtte ved lansering for én bruker.
 *
 *
 * @param søknadssaksopplysning Sier noe om bruker har svart at hen mottar livsoppholdytelser i søknaden.
 * @param saksbehandlerSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param avklartSaksopplysning Sier noe om hvilken saksopplysning som er gjeldende; subsumsjonen. For livsoppholdytelser er dette til og begynne med alltid saksbehandler.
 * @param vurderingsPeriode Vurderingsperioden faktumene må si noe om.
 *
 */
data class LivsoppholdVilkår private constructor(
    val søknadssaksopplysning: LivsoppholdSaksopplysning,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysning?,
    val avklartSaksopplysning: LivsoppholdSaksopplysning?,
    val vurderingsPeriode: Periode,
) : SkalErstatteVilkår {
    override val lovreferanse = Lovreferanse.LIVSOPPHOLDYTELSER

    override fun utfall(): Periodisering<Utfall2> {
        return when {
            avklartSaksopplysning == null -> {
                Periodisering(
                    Utfall2.UAVKLART,
                    vurderingsPeriode,
                )
            }
            avklartSaksopplysning.harLivsoppholdYtelser -> {
                Periodisering(
                    Utfall2.OPPFYLT,
                    vurderingsPeriode,
                )
            }
            !avklartSaksopplysning.harLivsoppholdYtelser -> {
                Periodisering(
                    Utfall2.IKKE_OPPFYLT,
                    vurderingsPeriode,
                )
            }
            else -> throw IllegalStateException("Ugyldig utfall")
        }
    }

    init {
        if (avklartSaksopplysning != null) require(avklartSaksopplysning.periode.inneholderHele(vurderingsPeriode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }
        require(søknadssaksopplysning.periode.inneholderHele(vurderingsPeriode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }
        if (saksbehandlerSaksopplysning != null) {
            require(saksbehandlerSaksopplysning.periode.inneholderHele(vurderingsPeriode)) { "Saksopplysningnen må dekke hele vurderingsperioden" }

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
        if (!vurderingsPeriode.inneholderHele(command.harYtelseForPeriode.periode)) {
            return PeriodenMåVæreLikVurderingsperioden.left()
        }
        val livsoppholdSaksopplysning =
            LivsoppholdSaksopplysning.Saksbehandler(
                harLivsoppholdYtelser = command.harYtelseForPeriode.harYtelse,
                årsakTilEndring = command.årsakTilEndring,
                tidsstempel = LocalDateTime.now(),
                saksbehandler = command.saksbehandler,
                periode = vurderingsPeriode,
            )
        return this.copy(
            saksbehandlerSaksopplysning = livsoppholdSaksopplysning,
            avklartSaksopplysning = livsoppholdSaksopplysning,
        ).right()
    }

    companion object {
        fun opprett(
            livsoppholdSaksopplysning: LivsoppholdSaksopplysning,
            vurderingsPeriode: Periode,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = livsoppholdSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = null,
                vurderingsPeriode = vurderingsPeriode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: LivsoppholdSaksopplysning,
            saksbehandlerSaksopplysning: LivsoppholdSaksopplysning?,
            avklartSaksopplysning: LivsoppholdSaksopplysning?,
            vurderingsPeriode: Periode,
            utfall: Periodisering<Utfall2>,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                søknadssaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                vurderingsPeriode = vurderingsPeriode,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
