package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AAPDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AlderspensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.DagpengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.ForeldrepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.GjenlevendepensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.JobbsjansenDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OmsorgspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OpplæringspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OvergangsstønadDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PensjonsinntektDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerNærståendeDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerSyktBarnDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadAlderDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadFlyktningDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SvangerskapspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SykepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.UføretrygdDelVilkår

data class LivsoppholdVilkår private constructor(
    val vurderingsperiode: Periode,
    val delVilkår: Map<LivsoppholdsytelseType, LivsoppholdDelVilkår>,
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

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): LivsoppholdVilkår {
        val nyeDelVilkår: Map<LivsoppholdsytelseType, LivsoppholdDelVilkår> = oppdaterDelVilkår(command)
        return this.copy(
            delVilkår = nyeDelVilkår,
            utfall = vurderMaskinelt(nyeDelVilkår.values),
        )
    }

    init {
        // TOOD: Sjekke at vi har med alle delvilkårene vi skal ha

        require(vurderingsperiode == utfall.totalePeriode) {
            "vurderingsperioden ($vurderingsperiode) og utfallet sin totale periode(${utfall.totalePeriode}) må være like."
        }
        delVilkår.forEach {
            require(vurderingsperiode == it.value.vurderingsperiode) {
                "vurderingsperioden ($vurderingsperiode) og delvilkåret for ${it.key} sin vurderingsperiode(${it.value.vurderingsperiode}) må være like."
            }
        }
    }

    companion object {

        fun opprett(
            vurderingsperiode: Periode,
            livsoppholdSaksopplysningerFraSøknad: LivsoppholdSaksopplysningerFraSøknad,
        ): LivsoppholdVilkår {
            // @formatter:off
            val delvilkår = mapOf(
                LivsoppholdsytelseType.ALDERSPENSJON to AlderspensjonDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.alderspensjonSøknadSaksopplysning),
                LivsoppholdsytelseType.GJENLEVENDEPENSJON to GjenlevendepensjonDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.gjenlevendeSøknadSaksopplysning),
                LivsoppholdsytelseType.SYKEPENGER to SykepengerDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.sykepengerSøknadSaksopplysning),
                LivsoppholdsytelseType.JOBBSJANSEN to JobbsjansenDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.jobbsjansenSøknadSaksopplysning),
                LivsoppholdsytelseType.PENSJONSINNTEKT to PensjonsinntektDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.pensjonsinntektSøknadSaksopplysning),
                LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER to SupplerendestønadAlderDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.supplerendestønadAlderSøknadSaksopplysning),
                LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING to SupplerendestønadFlyktningDelVilkår(vurderingsperiode, livsoppholdSaksopplysningerFraSøknad.supplerendestønadFlyktningSøknadSaksopplysning),
                LivsoppholdsytelseType.AAP to AAPDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.DAGPENGER to DagpengerDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.FORELDREPENGER to ForeldrepengerDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.OMSORGSPENGER to OmsorgspengerDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.OPPLÆRINGSPENGER to OpplæringspengerDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.OVERGANGSSTØNAD to OvergangsstønadDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE to PleiepengerNærståendeDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN to PleiepengerSyktBarnDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.SVANGERSKAPSPENGER to SvangerskapspengerDelVilkår(vurderingsperiode),
                LivsoppholdsytelseType.UFØRETRYGD to UføretrygdDelVilkår(vurderingsperiode)
            )
            // @formatter:on
            return LivsoppholdVilkår(
                vurderingsperiode = vurderingsperiode,
                delVilkår = delvilkår,
                utfall = vurderMaskinelt(delvilkår.values),
            )
        }

        fun vurderMaskinelt(
            delVilkår: Collection<LivsoppholdDelVilkår>,
        ): Periodisering<Utfall2> =
            delVilkår
                .map { it.utfall }
                .reduser(::kombinerDelVilkår)

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
            delVilkår: Set<LivsoppholdDelVilkår>,
            utfall: Periodisering<Utfall2>,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(
                vurderingsperiode = vurderingsperiode,
                delVilkår = delVilkår.associateBy { it.livsoppholdytelseType },
                utfall = utfall,
            )
        }
    }
}
