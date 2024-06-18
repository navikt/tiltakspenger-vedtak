package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.Inngangsvilkårsbehandling

data class LivsoppholdVilkår private constructor(
    val vurderingsperiode: Periode,
    val livsoppholdYtelser: Map<LivsoppholdDelVilkår, LivsoppholdYtelseDelVilkår>,
    val vurdering: Vurdering,
) : Inngangsvilkårsbehandling {

    override fun vilkår(): Inngangsvilkår {
        return Inngangsvilkår.LIVSOPPHOLDSYTELSER
    }

    override fun vurdering(): Vurdering = vurdering

    fun oppdaterSaksopplysninger(ytelseSaksopplysning: LivsoppholdYtelseSaksopplysning): LivsoppholdVilkår {
        require(livsoppholdYtelser.containsKey(ytelseSaksopplysning.vilkår)) { "Saksopplysning med vilkår ${ytelseSaksopplysning.vilkår} matcher ingen ytelse" }

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
            livsoppholdYtelser = livsoppholdYtelser + (
                ytelseSaksopplysning.vilkår to livsoppholdYtelser[ytelseSaksopplysning.vilkår]!!
                    .oppdaterSaksopplysning(ytelseSaksopplysning)
                ),
        ).vilkårsvurder()
    }

    private fun vilkårsvurder(): LivsoppholdVilkår {
        return this.copy(
            vurdering = vilkårsvurder(this.vurderingsperiode, this.livsoppholdYtelser),
        )
    }

    companion object {

        val alleDelVilkår: Set<LivsoppholdDelVilkår> = setOf(
            LivsoppholdDelVilkår.FORELDREPENGER,
            LivsoppholdDelVilkår.PLEIEPENGER_SYKT_BARN,
            LivsoppholdDelVilkår.PLEIEPENGER_NÆRSTÅENDE,
            LivsoppholdDelVilkår.ALDERSPENSJON,
            LivsoppholdDelVilkår.PENSJONSINNTEKT,
            LivsoppholdDelVilkår.ETTERLØNN,
            LivsoppholdDelVilkår.AAP,
            LivsoppholdDelVilkår.DAGPENGER,
            LivsoppholdDelVilkår.GJENLEVENDEPENSJON,
            LivsoppholdDelVilkår.FORELDREPENGER,
            LivsoppholdDelVilkår.JOBBSJANSEN,
            LivsoppholdDelVilkår.UFØRETRYGD,
            LivsoppholdDelVilkår.OMSORGSPENGER,
            LivsoppholdDelVilkår.OPPLÆRINGSPENGER,
            LivsoppholdDelVilkår.OVERGANGSSTØNAD,
            LivsoppholdDelVilkår.SYKEPENGER,
            LivsoppholdDelVilkår.SVANGERSKAPSPENGER,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADALDER,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADFLYKTNING,
        )

        operator fun invoke(vurderingsperiode: Periode): LivsoppholdVilkår {
            val vilkårDataPerVilkår = alleDelVilkår.associateWith { LivsoppholdYtelseDelVilkår(vurderingsperiode, it) }
            return LivsoppholdVilkår(
                vurderingsperiode,
                vilkårDataPerVilkår,
                vilkårsvurder(vurderingsperiode, vilkårDataPerVilkår),
            )
        }

        private fun vilkårsvurder(
            vurderingsperiode: Periode,
            vilkårDataPerVilkår: Map<LivsoppholdDelVilkår, LivsoppholdYtelseDelVilkår>,
        ): Vurdering {
            return Vurdering(
                utfall = vilkårDataPerVilkår.values.fold(
                    Periodisering(Utfall.UAVKLART, vurderingsperiode),
                ) { periodisering, ytelse ->
                    periodisering.kombiner(ytelse.vurdering.utfall, Companion::kombinerToUtfall)
                },
                detaljer = "",
            )
        }

        fun kombinerToUtfall(utfall1: Utfall, utfall2: Utfall): Utfall =
            when {
                utfall1 == Utfall.IKKE_OPPFYLT || utfall2 == Utfall.IKKE_OPPFYLT -> Utfall.IKKE_OPPFYLT
                utfall1 == Utfall.UAVKLART || utfall2 == Utfall.UAVKLART -> Utfall.UAVKLART
                else -> Utfall.OPPFYLT
            }

        fun fromDb(
            vurderingsperiode: Periode,
            korrigerbareYtelser: Map<LivsoppholdDelVilkår, LivsoppholdYtelseDelVilkår>,
            vurdering: Vurdering,
        ): LivsoppholdVilkår {
            return LivsoppholdVilkår(vurderingsperiode, korrigerbareYtelser, vurdering)
        }
    }
}
