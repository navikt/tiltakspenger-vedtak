package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Kategori
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

/*
TODO: Denne inneholder pt også data om ting som ikke er livsopphold, nemlig alder, intro, kvp og institusjonsopphold.
TODO: Dette må dras ut i egne klasser.
 */
data class LivsoppholdVilkårData private constructor(
    val vurderingsperiode: Periode,
    val korrigerbareYtelser: Map<Vilkår, KorrigerbarLivsopphold>,
) {

    fun håndterSøknad(søknad: Søknad): LivsoppholdVilkårData {
        val periodeSpmISøknaden = mapOf(
            Vilkår.KVP to søknad.kvp,
            Vilkår.INTROPROGRAMMET to søknad.intro,
            Vilkår.INSTITUSJONSOPPHOLD to søknad.institusjon,
            Vilkår.GJENLEVENDEPENSJON to søknad.gjenlevendepensjon,
            Vilkår.SYKEPENGER to søknad.sykepenger,
            Vilkår.SUPPLERENDESTØNADALDER to søknad.supplerendeStønadAlder,
            Vilkår.SUPPLERENDESTØNADFLYKTNING to søknad.supplerendeStønadFlyktning,
            Vilkår.JOBBSJANSEN to søknad.jobbsjansen,
            Vilkår.PENSJONSINNTEKT to søknad.trygdOgPensjon,
        )

        return this
            .copy(
                korrigerbareYtelser = periodeSpmISøknaden.keys
                    .fold(korrigerbareYtelser) { result, vilkår ->
                        result + (
                            vilkår to result[vilkår]!!.lagSaksopplysningFraPeriodeSpørsmål(
                                vilkår,
                                periodeSpmISøknaden[vilkår]!!,
                                søknad.vurderingsperiode(),
                            )
                            )
                    },
            )
            .copy(
                korrigerbareYtelser = korrigerbareYtelser + (
                    Vilkår.ALDERSPENSJON to korrigerbareYtelser[Vilkår.ALDERSPENSJON]!!.lagSaksopplysningFraFraOgMedDatospørsmål(
                        Vilkår.ALDERSPENSJON,
                        søknad.alderspensjon,
                        søknad.vurderingsperiode(),
                    )
                    ),
            )
            .copy(
                korrigerbareYtelser = korrigerbareYtelser + (
                    Vilkår.ETTERLØNN to korrigerbareYtelser[Vilkår.ETTERLØNN]!!.lagSaksopplysningFraJaNeiSpørsmål(
                        Vilkår.ETTERLØNN,
                        søknad.etterlønn,
                        søknad.vurderingsperiode(),
                    )
                    ),
            )
    }

    fun oppdaterSaksopplysninger(livsoppholdSaksopplysning: LivsoppholdSaksopplysning): LivsoppholdVilkårData {
        require(korrigerbareYtelser.containsKey(livsoppholdSaksopplysning.vilkår)) { "Saksopplysning med vilkår ${livsoppholdSaksopplysning.vilkår} matcher ingen ytelse" }

        /*
         fun List<Saksopplysning>.oppdaterSaksopplysninger(saksopplysning: Saksopplysning) =
        if (saksopplysning.kilde != Kilde.SAKSB) {
            if (this.first { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB } == saksopplysning) {
                this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB }
            } else {
                this.filterNot { it.vilkår == saksopplysning.vilkår }
            }
        } else {
            this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde == Kilde.SAKSB }
        }.plus(saksopplysning)
         */
        return this.copy(
            korrigerbareYtelser = korrigerbareYtelser + (
                livsoppholdSaksopplysning.vilkår to korrigerbareYtelser[livsoppholdSaksopplysning.vilkår]!!
                    .oppdaterSaksopplysning(livsoppholdSaksopplysning)
                ),
        )
    }

    fun samletUtfall(): Periodisering<Utfall> {
        return samletUtfallPerKategori().values.toList().reduser(::kombinerToUtfall)
    }

    fun samletUtfallPerKategori(): Map<Kategori, Periodisering<Utfall>> {
        val alder = Kategori.ALDER.vilkår.fold(
            Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode),
        ) { periodisering, vilkår ->
            periodisering.kombiner(korrigerbareYtelser[vilkår]!!.vurdering.utfall, ::kombinerToUtfall)
        }

        val inst = Kategori.INSTITUSJONSOPPHOLD.vilkår.fold(
            Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode),
        ) { periodisering, vilkår ->
            periodisering.kombiner(korrigerbareYtelser[vilkår]!!.vurdering.utfall, ::kombinerToUtfall)
        }

        val introOgKvp = Kategori.INTROKVP.vilkår.fold(
            Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode),
        ) { periodisering, vilkår ->
            periodisering.kombiner(korrigerbareYtelser[vilkår]!!.vurdering.utfall, ::kombinerToUtfall)
        }

        val utbetalinger = Kategori.UTBETALINGER.vilkår.fold(
            Periodisering(Utfall.KREVER_MANUELL_VURDERING, vurderingsperiode),
        ) { periodisering, vilkår ->
            periodisering.kombiner(korrigerbareYtelser[vilkår]!!.vurdering.utfall, ::kombinerToUtfall)
        }

        return mapOf(
            Kategori.ALDER to alder,
            Kategori.INSTITUSJONSOPPHOLD to inst,
            Kategori.INTROKVP to introOgKvp,
            Kategori.UTBETALINGER to utbetalinger,
        )
    }

    companion object {

        val alleVilkår: List<Vilkår> =
            Kategori.ALDER.vilkår +
                Kategori.INTROKVP.vilkår +
                Kategori.INSTITUSJONSOPPHOLD.vilkår +
                Kategori.UTBETALINGER.vilkår

        operator fun invoke(vurderingsperiode: Periode): LivsoppholdVilkårData {
            return LivsoppholdVilkårData(
                vurderingsperiode,
                alleVilkår.associateWith { KorrigerbarLivsopphold(vurderingsperiode, it) },
            )
        }

        fun kombinerToUtfall(utfall1: Utfall, utfall2: Utfall): Utfall =
            when {
                utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                utfall1 == Utfall.KREVER_MANUELL_VURDERING || utfall2 == Utfall.KREVER_MANUELL_VURDERING -> Utfall.KREVER_MANUELL_VURDERING
                else -> Utfall.OPPFYLT
            }

        fun fromDb(
            vurderingsperiode: Periode,
            korrigerbareYtelser: Map<Vilkår, KorrigerbarLivsopphold>,
        ): LivsoppholdVilkårData {
            return LivsoppholdVilkårData(vurderingsperiode, korrigerbareYtelser)
        }
    }
}
