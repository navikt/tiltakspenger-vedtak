package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

// TODO: Vurdere om delvilkårene heller skal være i en Map<Livsoppholdsytelse, LivsoppholdDelVilkår> ?
data class LivsoppholdVilkaar private constructor(
    val vurderingsperiode: Periode,
    val aapDelVilkår: AAPDelVilkaar,
    val alderspensjonDelVilkår: AlderspensjonDelVilkaar,
    val utfall: Periodisering<Utfall2>,
) : SkalErstatteVilkår {

    val samletUtfall: SamletUtfall = when {
        utfall.perioder().any { it.verdi == Utfall2.UAVKLART } -> SamletUtfall.UAVKLART
        utfall.perioder().all { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.OPPFYLT
        utfall.perioder().all { it.verdi == Utfall2.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
        utfall.perioder().any() { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
        else -> throw IllegalStateException("Ugyldig utfall")
    }

    override val lovreferanse = Lovreferanse.LIVSOPPHOLD

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): LivsoppholdVilkaar {
        when (command.livsoppholdsytelse) {
            Livsoppholdsytelse.AAP -> {
                val saksopplysning = AAPSaksopplysning.Saksbehandler(
                    harYtelse = Periodisering(
                        command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                    ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                    årsakTilEndring = command.årsakTilEndring,
                    saksbehandler = command.saksbehandler,
                    tidsstempel = LocalDateTime.now(),
                )
                return this.copy(
                    aapDelVilkår = aapDelVilkår.leggTilSaksopplysning(saksopplysning),
                    utfall = vurderMaskinelt(aapDelVilkår, alderspensjonDelVilkår),
                )
            }

            Livsoppholdsytelse.DAGPENGER -> TODO()
            Livsoppholdsytelse.ALDERSPENSJON -> TODO()
            Livsoppholdsytelse.GJENLEVENDEPENSJON -> TODO()
            Livsoppholdsytelse.SYKEPENGER -> TODO()
            Livsoppholdsytelse.JOBBSJANSEN -> TODO()
            Livsoppholdsytelse.FORELDREPENGER -> TODO()
            Livsoppholdsytelse.OMSORGSPENGER -> TODO()
            Livsoppholdsytelse.OPPLÆRINGSPENGER -> TODO()
        }
    }

    init {
        require(vurderingsperiode == aapDelVilkår.vurderingsperiode) {
            "vurderingsperioden ($vurderingsperiode) og delvilkåret for aap sin vurderingsperiode(${aapDelVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == alderspensjonDelVilkår.vurderingsperiode) {
            "vurderingsperioden ($vurderingsperiode) og delvilkåret for alderspensjon sin vurderingsperiode(${aapDelVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == utfall.totalePeriode) {
            "vurderingsperioden ($vurderingsperiode) og utfallet sin totale periode(${utfall.totalePeriode}) må være like."
        }
    }

    companion object {

        fun opprett(
            vurderingsperiode: Periode,
            alderspensjonSøknadSaksopplysning: AlderspensjonSaksopplysning.Søknad,
            // TODO: Her kommer alle saksopplysningene fra søknaden
        ): LivsoppholdVilkaar {
            val aapDelVilkår = AAPDelVilkaar(vurderingsperiode)
            val alderspensjonDelVilkår = AlderspensjonDelVilkaar(vurderingsperiode, alderspensjonSøknadSaksopplysning)
            return LivsoppholdVilkaar(
                vurderingsperiode = vurderingsperiode,
                aapDelVilkår = AAPDelVilkaar(vurderingsperiode),
                alderspensjonDelVilkår = AlderspensjonDelVilkaar(vurderingsperiode, alderspensjonSøknadSaksopplysning),
                utfall = vurderMaskinelt(aapDelVilkår, alderspensjonDelVilkår),
            )
        }

        fun vurderMaskinelt(
            aapDelVilkår: AAPDelVilkaar,
            alderspensjonDelVilkår: AlderspensjonDelVilkaar,
        ): Periodisering<Utfall2> =
            listOf(aapDelVilkår.utfall, alderspensjonDelVilkår.utfall).reduser(::kombinerDelVilkår)

        fun kombinerDelVilkår(utfall1: Utfall2, utfall2: Utfall2): Utfall2 =
            when {
                utfall1 == Utfall2.IKKE_OPPFYLT || utfall2 == Utfall2.IKKE_OPPFYLT -> Utfall2.IKKE_OPPFYLT
                utfall1 == Utfall2.UAVKLART || utfall2 == Utfall2.UAVKLART -> Utfall2.UAVKLART
                else -> Utfall2.OPPFYLT
            }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            vurderingsperiode: Periode,
            aapDelVilkår: AAPDelVilkaar,
            alderspensjonDelVilkår: AlderspensjonDelVilkaar,
            utfall: Periodisering<Utfall2>,
        ): LivsoppholdVilkaar {
            return LivsoppholdVilkaar(
                vurderingsperiode = vurderingsperiode,
                aapDelVilkår = aapDelVilkår,
                alderspensjonDelVilkår = alderspensjonDelVilkår,
                utfall = utfall,
            )
        }
    }
}
