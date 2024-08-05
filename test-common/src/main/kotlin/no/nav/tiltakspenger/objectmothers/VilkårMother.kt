package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import java.time.LocalDate
import java.time.LocalDateTime

interface VilkårMother {

    fun vilkårsett(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        institusjonsoppholdVilkår: InstitusjonsoppholdVilkår = institusjonsoppholdVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        kvpVilkår: KVPVilkår = kvpVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        tiltakDeltagelseVilkår: TiltakDeltagelseVilkår = tiltakDeltagelseVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        introVilkår: IntroVilkår = introVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        livsoppholdVilkår: LivsoppholdVilkår = livsoppholdVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        alderVilkår: AlderVilkår = alderVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
        kravfristVilkår: KravfristVilkår = kravfristVilkår(
            vurderingsperiode = vurderingsperiode,
        ),
    ): Vilkårssett {
        return Vilkårssett(
            vurderingsperiode = vurderingsperiode,
            institusjonsoppholdVilkår = institusjonsoppholdVilkår,
            kvpVilkår = kvpVilkår,
            tiltakDeltagelseVilkår = tiltakDeltagelseVilkår,
            introVilkår = introVilkår,
            livsoppholdVilkår = livsoppholdVilkår,
            alderVilkår = alderVilkår,
            kravfristVilkår = kravfristVilkår,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun institusjonsoppholdVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        opphold: Periodisering<Opphold> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Opphold.IKKE_OPPHOLD,
                    vurderingsperiode,
                ),
            ),
        ),
        søknadSaksopplysning: InstitusjonsoppholdSaksopplysning.Søknad = institusjonsoppholdSøknadSaksopplysning(
            vurderingsperiode = vurderingsperiode,
            opphold = opphold,
        ),
        // saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysning.Saksbehandler? = institusjonsoppholdSaksbehandlerSaksopplysning(),
    ): InstitusjonsoppholdVilkår {
        return InstitusjonsoppholdVilkår.opprett(
            vurderingsperiode = vurderingsperiode,
            søknadSaksopplysning = søknadSaksopplysning,
        )
    }

    fun institusjonsoppholdSøknadSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        opphold: Periodisering<Opphold> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Opphold.IKKE_OPPHOLD,
                    vurderingsperiode,
                ),
            ),
        ),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
    ): InstitusjonsoppholdSaksopplysning.Søknad {
        return InstitusjonsoppholdSaksopplysning.Søknad(
            opphold = opphold,
            tidsstempel = tidsstempel,
        )
    }

    @Suppress("unused")
    fun institusjonsoppholdSaksbehandlerSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        opphold: Periodisering<Opphold> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Opphold.IKKE_OPPHOLD,
                    vurderingsperiode,
                ),
            ),
        ),
        årsakTilEndring: ÅrsakTilEndring = ÅrsakTilEndring.FEIL_I_INNHENTET_DATA,
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): InstitusjonsoppholdSaksopplysning.Saksbehandler {
        return InstitusjonsoppholdSaksopplysning.Saksbehandler(
            opphold = opphold,
            årsakTilEndring = årsakTilEndring,
            tidsstempel = tidsstempel,
            saksbehandler = saksbehandler,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun kvpVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        deltar: Periodisering<Deltagelse> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Deltagelse.DELTAR_IKKE,
                    vurderingsperiode,
                ),
            ),
        ),
        søknadSaksopplysning: KvpSaksopplysning.Søknad = kvpSøknadSaksopplysning(deltar = deltar),
    ): KVPVilkår {
        return KVPVilkår.opprett(
            vurderingsperiode = vurderingsperiode,
            søknadSaksopplysning = søknadSaksopplysning,
        )
    }

    fun kvpSøknadSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        deltar: Periodisering<Deltagelse> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Deltagelse.DELTAR_IKKE,
                    vurderingsperiode,
                ),
            ),
        ),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
    ): KvpSaksopplysning.Søknad {
        return KvpSaksopplysning.Søknad(
            deltar = deltar,
            tidsstempel = tidsstempel,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun introVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        deltar: Periodisering<Deltagelse> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Deltagelse.DELTAR_IKKE,
                    vurderingsperiode,
                ),
            ),
        ),
        søknadSaksopplysning: IntroSaksopplysning.Søknad = introSøknadSaksopplysning(deltar = deltar),
    ): IntroVilkår {
        return IntroVilkår.opprett(
            vurderingsperiode = vurderingsperiode,
            søknadSaksopplysning = søknadSaksopplysning,
        )
    }

    fun introSøknadSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        deltar: Periodisering<Deltagelse> = Periodisering(
            listOf(
                PeriodeMedVerdi(
                    Deltagelse.DELTAR_IKKE,
                    vurderingsperiode,
                ),
            ),
        ),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
    ): IntroSaksopplysning.Søknad {
        return IntroSaksopplysning.Søknad(
            deltar = deltar,
            tidsstempel = tidsstempel,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun alderVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        registerSaksopplysning: AlderSaksopplysning.Register = alderRegisterSaksopplysning(fødselsdato = fødselsdato),
    ): AlderVilkår {
        return AlderVilkår.opprett(
            registerSaksopplysning = registerSaksopplysning,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun alderRegisterSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
    ): AlderSaksopplysning.Register {
        return AlderSaksopplysning.Register(
            fødselsdato = fødselsdato,
            tidsstempel = tidsstempel,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun tiltakDeltagelseVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        tiltakNavn: String = "Tiltaksnavnet",
        girRett: Boolean = true,
        status: String = "Gjennomføres",
        kilde: String = "Komet",
        registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register = tiltakDeltagelseSaksopplysning(
            vurderingsperiode = vurderingsperiode,
            fødselsdato = fødselsdato,
            tidsstempel = tidsstempel,
            tiltakNavn = tiltakNavn,
            girRett = girRett,
            status = status,
            kilde = kilde,
        ),
    ): TiltakDeltagelseVilkår {
        return TiltakDeltagelseVilkår.opprett(
            registerSaksopplysning = registerSaksopplysning,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun tiltakDeltagelseSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        tiltakNavn: String = "Tiltaksnavnet",
        girRett: Boolean = true,
        status: String = "Gjennomføres",
        kilde: String = "Komet",
    ): TiltakDeltagelseSaksopplysning.Register {
        return TiltakDeltagelseSaksopplysning.Register(
            tidsstempel = tidsstempel,
            tiltakNavn = tiltakNavn,
            deltagelsePeriode = vurderingsperiode,
            girRett = girRett,
            status = status,
            kilde = kilde,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun livsoppholdVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        harLivsoppholdYtelser: Boolean = false,
        årsakTilEndring: ÅrsakTilEndring = ÅrsakTilEndring.FEIL_I_INNHENTET_DATA,
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
        behandlingId: BehandlingId = BehandlingId.random(),
        søknadSaksopplysning: LivsoppholdSaksopplysning.Søknad = livsoppholdSøknadSaksopplysning(
            vurderingsperiode = vurderingsperiode,
            tidsstempel = tidsstempel,
            fødselsdato = fødselsdato,
            harLivsoppholdYtelser = harLivsoppholdYtelser,
        ),
        saksopplysningCommand: LeggTilLivsoppholdSaksopplysningCommand? = LeggTilLivsoppholdSaksopplysningCommand(
            behandlingId = behandlingId,
            saksbehandler = saksbehandler,
            harYtelseForPeriode = LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                periode = vurderingsperiode,
                harYtelse = harLivsoppholdYtelser,
            ),
            årsakTilEndring = årsakTilEndring,
        ),
    ): LivsoppholdVilkår {
        return LivsoppholdVilkår.opprett(
            søknadSaksopplysning = søknadSaksopplysning,
            vurderingsperiode = vurderingsperiode,
        ).let {
            if (saksopplysningCommand != null) {
                it.leggTilSaksbehandlerSaksopplysning(saksopplysningCommand).getOrNull()!!
            } else {
                it
            }
        }
    }

    fun livsoppholdSøknadSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        harLivsoppholdYtelser: Boolean = false,
    ): LivsoppholdSaksopplysning.Søknad {
        return LivsoppholdSaksopplysning.Søknad(
            tidsstempel = tidsstempel,
            harLivsoppholdYtelser = harLivsoppholdYtelser,
            periode = vurderingsperiode,
        )
    }

    fun livsoppholdSaksbehandlerSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        harLivsoppholdYtelser: Boolean = false,
        årsakTilEndring: ÅrsakTilEndring = ÅrsakTilEndring.FEIL_I_INNHENTET_DATA,
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): LivsoppholdSaksopplysning.Saksbehandler {
        return LivsoppholdSaksopplysning.Saksbehandler(
            tidsstempel = tidsstempel,
            harLivsoppholdYtelser = harLivsoppholdYtelser,
            årsakTilEndring = årsakTilEndring,
            periode = vurderingsperiode,
            saksbehandler = saksbehandler,
        )
    }

    /**
     * Støtter ikke saksbehandlerSaksopplysning siden det ikke skal implementeres i MVP.
     */
    fun kravfristVilkår(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        kravdato: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        søknadSaksopplysning: KravfristSaksopplysning.Søknad = kravfristSøknadSaksopplysning(
            vurderingsperiode = vurderingsperiode,
            tidsstempel = tidsstempel,
            kravdato = kravdato,
        ),
    ): KravfristVilkår {
        return KravfristVilkår.opprett(
            søknadSaksopplysning = søknadSaksopplysning,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun kravfristSøknadSaksopplysning(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        tidsstempel: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
        kravdato: LocalDateTime = vurderingsperiode.fraOgMed.atStartOfDay(),
    ): KravfristSaksopplysning.Søknad {
        return KravfristSaksopplysning.Søknad(
            tidsstempel = tidsstempel,
            kravdato = kravdato,
        )
    }
}
