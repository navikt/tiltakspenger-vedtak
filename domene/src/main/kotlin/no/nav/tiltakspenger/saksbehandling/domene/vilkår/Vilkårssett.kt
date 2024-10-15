package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import arrow.core.Either
import arrow.core.getOrElse
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning.Register
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.institusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.introSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.LeggTilKravfristSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.kravfristSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.kvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.livsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.tilRegisterSaksopplysning
import java.time.LocalDate

/**
 * Ref til begrepskatalogen.
 * Vil både være inngangsvilkår og andre vilkår.
 * Det totale settet vilkår.
 */
data class Vilkårssett(
    val vurderingsperiode: Periode,
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkår,
    val kvpVilkår: KVPVilkår,
    val tiltakDeltagelseVilkår: TiltakDeltagelseVilkår,
    val introVilkår: IntroVilkår,
    val livsoppholdVilkår: LivsoppholdVilkår,
    val alderVilkår: AlderVilkår,
    val kravfristVilkår: KravfristVilkår,
) {
    private val vilkårliste: List<Vilkår> =
        listOf(
            institusjonsoppholdVilkår,
            kvpVilkår,
            tiltakDeltagelseVilkår,
            introVilkår,
            livsoppholdVilkår,
            alderVilkår,
            kravfristVilkår,
        )

    val samletUtfall: SamletUtfall =
        when {
            vilkårliste.any { it.samletUtfall() == SamletUtfall.UAVKLART } -> SamletUtfall.UAVKLART
            vilkårliste.all { it.samletUtfall() == SamletUtfall.OPPFYLT } -> SamletUtfall.OPPFYLT
            vilkårliste.all { it.samletUtfall() == SamletUtfall.IKKE_OPPFYLT } -> throw IllegalStateException("Støtter ikke avslag enda")
            else -> throw IllegalStateException("Støtter ikke delvis oppfylt enda")
        }

    fun utfallsperioder(): Periodisering<UtfallForPeriode> =
        vilkårliste.fold(
            Periodisering(UtfallForPeriode.OPPFYLT, vurderingsperiode),
        ) { total, vilkår -> total.kombiner(vilkår.utfall, UtfallForPeriode::kombiner).slåSammenTilstøtendePerioder() }

    init {
        require(vurderingsperiode == institusjonsoppholdVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og institusjonsoppholdVilkår.vurderingsperiode(${institusjonsoppholdVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == kvpVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og kvpVilkår.vurderingsperiode(${kvpVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == tiltakDeltagelseVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og tiltakDeltagelseVilkår.vurderingsperiode(${tiltakDeltagelseVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == introVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og introVilkår.vurderingsperiode(${introVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == livsoppholdVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og livsoppholdVilkår.vurderingsperiode(${livsoppholdVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == alderVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og alderVilkår.vurderingsperiode(${alderVilkår.vurderingsperiode}) må være like."
        }
        require(vurderingsperiode == kravfristVilkår.vurderingsperiode) {
            "vurderingsperiode($vurderingsperiode) og kravfristVilkår.vurderingsperiode(${kravfristVilkår.vurderingsperiode}) må være like."
        }
    }

    fun oppdaterKVP(command: LeggTilKvpSaksopplysningCommand): Vilkårssett =
        this.copy(
            kvpVilkår = kvpVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )

    fun oppdaterIntro(command: LeggTilIntroSaksopplysningCommand): Vilkårssett =
        this.copy(
            introVilkår = introVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )

    fun oppdaterAlder(command: LeggTilAlderSaksopplysningCommand): Vilkårssett =
        this.copy(
            alderVilkår = alderVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )

    fun oppdaterKravdato(command: LeggTilKravfristSaksopplysningCommand): Vilkårssett =
        this.copy(
            kravfristVilkår = kravfristVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )

    fun oppdaterLivsopphold(command: LeggTilLivsoppholdSaksopplysningCommand): Either<PeriodenMåVæreLikVurderingsperioden, Vilkårssett> =
        livsoppholdVilkår.leggTilSaksbehandlerSaksopplysning(command).map {
            this.copy(livsoppholdVilkår = it)
        }

    companion object {
        fun opprett(
            søknad: Søknad,
            fødselsdato: LocalDate,
            tiltak: Tiltak,
            vurderingsperiode: Periode,
        ): Vilkårssett = Either.catch {
            Vilkårssett(
                vurderingsperiode = vurderingsperiode,
                institusjonsoppholdVilkår =
                InstitusjonsoppholdVilkår.opprett(
                    vurderingsperiode,
                    søknad.institusjonsoppholdSaksopplysning(
                        vurderingsperiode,
                    ),
                ),
                kvpVilkår = KVPVilkår.opprett(vurderingsperiode, søknad.kvpSaksopplysning(vurderingsperiode)),
                introVilkår = IntroVilkår.opprett(vurderingsperiode, søknad.introSaksopplysning(vurderingsperiode)),
                livsoppholdVilkår =
                LivsoppholdVilkår.opprett(
                    søknad.livsoppholdSaksopplysning(vurderingsperiode),
                    vurderingsperiode,
                ),
                alderVilkår =
                AlderVilkår.opprett(
                    Register.opprett(fødselsdato = fødselsdato),
                    vurderingsperiode,
                ),
                kravfristVilkår = KravfristVilkår.opprett(søknad.kravfristSaksopplysning(), vurderingsperiode),
                tiltakDeltagelseVilkår =
                TiltakDeltagelseVilkår.opprett(
                    vurderingsperiode = vurderingsperiode,
                    registerSaksopplysning = tiltak.tilRegisterSaksopplysning(),
                ),
            )
        }.getOrElse { throw it }
    }
}
