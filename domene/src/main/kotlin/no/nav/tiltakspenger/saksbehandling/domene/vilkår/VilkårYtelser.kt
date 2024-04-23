package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning

data class VilkårYtelser(
    val aap: VilkårDataYtelser,
    val alderspensjon: VilkårDataYtelser,
    val dagpenger: VilkårDataYtelser,
    val foreldrepenger: VilkårDataYtelser,
    val gjenlevendepensjon: VilkårDataYtelser,
    val institusjonsopphold: VilkårDataYtelser,
    val introprogrammet: VilkårDataYtelser,
    val jobbsjansen: VilkårDataYtelser,
    val kvp: VilkårDataYtelser,
    val omsorgspenger: VilkårDataYtelser,
    val opplæringspenger: VilkårDataYtelser,
    val overgangsstønad: VilkårDataYtelser,
    val pensjonsinntekt: VilkårDataYtelser,
    val pleiepengerNærstående: VilkårDataYtelser,
    val pleiepengerSyktBarn: VilkårDataYtelser,
    val supplerendestønadalder: VilkårDataYtelser,
    val supplerendestønadflyktning: VilkårDataYtelser,
    val svangerskapspenger: VilkårDataYtelser,
    val sykepenger: VilkårDataYtelser,
    val tiltakspenger: VilkårDataYtelser,
    val uføretrygd: VilkårDataYtelser,
    val etterlønn: VilkårDataYtelser,
    val tiltakdeltakelse: VilkårDataYtelser,
) {
    companion object {
        fun ytelser() = listOf(
            Vilkår.AAP,
            Vilkår.ALDERSPENSJON,
            Vilkår.DAGPENGER,
            Vilkår.FORELDREPENGER,
            Vilkår.GJENLEVENDEPENSJON,
            Vilkår.INSTITUSJONSOPPHOLD,
            Vilkår.INTROPROGRAMMET,
            Vilkår.JOBBSJANSEN,
            Vilkår.KVP,
            Vilkår.OMSORGSPENGER,
            Vilkår.OPPLÆRINGSPENGER,
            Vilkår.OVERGANGSSTØNAD,
            Vilkår.PENSJONSINNTEKT,
            Vilkår.PLEIEPENGER_NÆRSTÅENDE,
            Vilkår.PLEIEPENGER_SYKT_BARN,
            Vilkår.SUPPLERENDESTØNADALDER,
            Vilkår.SUPPLERENDESTØNADFLYKTNING,
            Vilkår.SVANGERSKAPSPENGER,
            Vilkår.SYKEPENGER,
            Vilkår.TILTAKSPENGER,
            Vilkår.UFØRETRYGD,
            Vilkår.ETTERLØNN,
            Vilkår.TILTAKDELTAKELSE,
        )

        fun opprettFraSøknad(søknad: Søknad): VilkårYtelser {
            val vurderingsperiode = søknad.vurderingsperiode()
            return VilkårYtelser(
                kvp = periodeSpørsmålVilkårFraSøknad(Vilkår.KVP, søknad.vurderingsperiode(), søknad.kvp),
                alderspensjon = fraOgMedSpørsmålVilkårFraSøknad(
                    Vilkår.ALDERSPENSJON,
                    vurderingsperiode,
                    søknad.alderspensjon,
                ),
                gjenlevendepensjon = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.GJENLEVENDEPENSJON,
                    vurderingsperiode,
                    søknad.gjenlevendepensjon,
                ),
                institusjonsopphold = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.INSTITUSJONSOPPHOLD,
                    vurderingsperiode,
                    søknad.institusjon,
                ),
                introprogrammet = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.INTROPROGRAMMET,
                    vurderingsperiode,
                    søknad.intro,
                ),
                jobbsjansen = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.JOBBSJANSEN,
                    vurderingsperiode,
                    søknad.jobbsjansen,
                ),
                pensjonsinntekt = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.PENSJONSINNTEKT,
                    vurderingsperiode,
                    søknad.trygdOgPensjon,
                ),
                supplerendestønadalder = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.SUPPLERENDESTØNADALDER,
                    vurderingsperiode,
                    søknad.supplerendeStønadAlder,
                ),
                supplerendestønadflyktning = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.SUPPLERENDESTØNADFLYKTNING,
                    vurderingsperiode,
                    søknad.supplerendeStønadFlyktning,
                ),
                sykepenger = periodeSpørsmålVilkårFraSøknad(
                    Vilkår.SYKEPENGER,
                    vurderingsperiode,
                    søknad.sykepenger,
                ),
                etterlønn = jaNeiSpørsmålVilkårFraSøknad(
                    Vilkår.ETTERLØNN,
                    vurderingsperiode,
                    søknad.etterlønn,
                ),

                aap = ikkeFraSøknad(Vilkår.AAP, vurderingsperiode),
                dagpenger = ikkeFraSøknad(Vilkår.DAGPENGER, vurderingsperiode),
                foreldrepenger = ikkeFraSøknad(Vilkår.FORELDREPENGER, vurderingsperiode),
                omsorgspenger = ikkeFraSøknad(Vilkår.OMSORGSPENGER, vurderingsperiode),
                opplæringspenger = ikkeFraSøknad(Vilkår.OPPLÆRINGSPENGER, vurderingsperiode),
                overgangsstønad = ikkeFraSøknad(Vilkår.OVERGANGSSTØNAD, vurderingsperiode),
                pleiepengerNærstående = ikkeFraSøknad(Vilkår.PLEIEPENGER_NÆRSTÅENDE, vurderingsperiode),
                pleiepengerSyktBarn = ikkeFraSøknad(Vilkår.PLEIEPENGER_SYKT_BARN, vurderingsperiode),
                svangerskapspenger = ikkeFraSøknad(Vilkår.SVANGERSKAPSPENGER, vurderingsperiode),
                tiltakspenger = ikkeFraSøknad(Vilkår.TILTAKSPENGER, vurderingsperiode),
                uføretrygd = ikkeFraSøknad(Vilkår.UFØRETRYGD, vurderingsperiode),
                tiltakdeltakelse = ikkeFraSøknad(Vilkår.TILTAKDELTAKELSE, vurderingsperiode),
            )
        }
    }

    fun leggTilSøknad(søknad: Søknad): VilkårYtelser {
        val søknadssaksopplysninger = opprettFraSøknad(søknad)
        alderspensjon.leggTilSaksopplysning(søknadssaksopplysninger.alderspensjon.saksopplysningerAnnet)
        gjenlevendepensjon.leggTilSaksopplysning(søknadssaksopplysninger.gjenlevendepensjon.saksopplysningerAnnet)
        institusjonsopphold.leggTilSaksopplysning(søknadssaksopplysninger.institusjonsopphold.saksopplysningerAnnet)
        introprogrammet.leggTilSaksopplysning(søknadssaksopplysninger.introprogrammet.saksopplysningerAnnet)
        jobbsjansen.leggTilSaksopplysning(søknadssaksopplysninger.jobbsjansen.saksopplysningerAnnet)
        kvp.leggTilSaksopplysning(søknadssaksopplysninger.kvp.saksopplysningerAnnet)
        pensjonsinntekt.leggTilSaksopplysning(søknadssaksopplysninger.pensjonsinntekt.saksopplysningerAnnet)
        supplerendestønadalder.leggTilSaksopplysning(søknadssaksopplysninger.supplerendestønadalder.saksopplysningerAnnet)
        supplerendestønadflyktning.leggTilSaksopplysning(søknadssaksopplysninger.supplerendestønadflyktning.saksopplysningerAnnet)
        sykepenger.leggTilSaksopplysning(søknadssaksopplysninger.sykepenger.saksopplysningerAnnet)
        etterlønn.leggTilSaksopplysning(søknadssaksopplysninger.etterlønn.saksopplysningerAnnet)
        return this
    }

    fun avklarFakta(): List<YtelseSaksopplysning> {
        return aap.vilkårsvurder().avklarFakta() +
            alderspensjon.vilkårsvurder().avklarFakta() +
            dagpenger.vilkårsvurder().avklarFakta() +
            foreldrepenger.vilkårsvurder().avklarFakta() +
            gjenlevendepensjon.vilkårsvurder().avklarFakta() +
            institusjonsopphold.vilkårsvurder().avklarFakta() +
            introprogrammet.vilkårsvurder().avklarFakta() +
            jobbsjansen.vilkårsvurder().avklarFakta() +
            kvp.vilkårsvurder().avklarFakta() +
            omsorgspenger.vilkårsvurder().avklarFakta() +
            opplæringspenger.vilkårsvurder().avklarFakta() +
            overgangsstønad.vilkårsvurder().avklarFakta() +
            pensjonsinntekt.vilkårsvurder().avklarFakta() +
            pleiepengerNærstående.vilkårsvurder().avklarFakta() +
            pleiepengerSyktBarn.vilkårsvurder().avklarFakta() +
            supplerendestønadalder.vilkårsvurder().avklarFakta() +
            supplerendestønadflyktning.vilkårsvurder().avklarFakta() +
            svangerskapspenger.vilkårsvurder().avklarFakta() +
            sykepenger.vilkårsvurder().avklarFakta() +
            tiltakspenger.vilkårsvurder().avklarFakta() +
            uføretrygd.vilkårsvurder().avklarFakta() +
            etterlønn.vilkårsvurder().avklarFakta() +
            tiltakdeltakelse.vilkårsvurder().avklarFakta()
    }

    fun vilkårsvurder(): List<Vurdering> {
        return aap.vilkårsvurder().vurderinger +
            alderspensjon.vilkårsvurder().vurderinger +
            dagpenger.vilkårsvurder().vurderinger +
            foreldrepenger.vilkårsvurder().vurderinger +
            gjenlevendepensjon.vilkårsvurder().vurderinger +
            institusjonsopphold.vilkårsvurder().vurderinger +
            introprogrammet.vilkårsvurder().vurderinger +
            jobbsjansen.vilkårsvurder().vurderinger +
            kvp.vilkårsvurder().vurderinger +
            omsorgspenger.vilkårsvurder().vurderinger +
            opplæringspenger.vilkårsvurder().vurderinger +
            overgangsstønad.vilkårsvurder().vurderinger +
            pensjonsinntekt.vilkårsvurder().vurderinger +
            pleiepengerNærstående.vilkårsvurder().vurderinger +
            pleiepengerSyktBarn.vilkårsvurder().vurderinger +
            supplerendestønadalder.vilkårsvurder().vurderinger +
            supplerendestønadflyktning.vilkårsvurder().vurderinger +
            svangerskapspenger.vilkårsvurder().vurderinger +
            sykepenger.vilkårsvurder().vurderinger +
            tiltakspenger.vilkårsvurder().vurderinger +
            uføretrygd.vilkårsvurder().vurderinger +
            etterlønn.vilkårsvurder().vurderinger +
            tiltakdeltakelse.vilkårsvurder().vurderinger
    }

    fun leggTilSaksopplysning(saksopplysning: List<YtelseSaksopplysning>) {
        val vilkår = saksopplysning.first().vilkår
        when (vilkår) {
            Vilkår.AAP -> aap.leggTilSaksopplysning(saksopplysning)
            Vilkår.ALDERSPENSJON -> alderspensjon.leggTilSaksopplysning(saksopplysning)
            Vilkår.DAGPENGER -> dagpenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.FORELDREPENGER -> foreldrepenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.GJENLEVENDEPENSJON -> gjenlevendepensjon.leggTilSaksopplysning(saksopplysning)
            Vilkår.INSTITUSJONSOPPHOLD -> institusjonsopphold.leggTilSaksopplysning(saksopplysning)
            Vilkår.INTROPROGRAMMET -> introprogrammet.leggTilSaksopplysning(saksopplysning)
            Vilkår.JOBBSJANSEN -> jobbsjansen.leggTilSaksopplysning(saksopplysning)
            Vilkår.KVP -> kvp.leggTilSaksopplysning(saksopplysning)
            Vilkår.OMSORGSPENGER -> omsorgspenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.OPPLÆRINGSPENGER -> opplæringspenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.OVERGANGSSTØNAD -> overgangsstønad.leggTilSaksopplysning(saksopplysning)
            Vilkår.PENSJONSINNTEKT -> pensjonsinntekt.leggTilSaksopplysning(saksopplysning)
            Vilkår.PLEIEPENGER_NÆRSTÅENDE -> pleiepengerNærstående.leggTilSaksopplysning(saksopplysning)
            Vilkår.PLEIEPENGER_SYKT_BARN -> pleiepengerSyktBarn.leggTilSaksopplysning(saksopplysning)
            Vilkår.SUPPLERENDESTØNADALDER -> supplerendestønadalder.leggTilSaksopplysning(saksopplysning)
            Vilkår.SUPPLERENDESTØNADFLYKTNING -> supplerendestønadflyktning.leggTilSaksopplysning(saksopplysning)
            Vilkår.SVANGERSKAPSPENGER -> svangerskapspenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.SYKEPENGER -> sykepenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.TILTAKSPENGER -> tiltakspenger.leggTilSaksopplysning(saksopplysning)
            Vilkår.UFØRETRYGD -> uføretrygd.leggTilSaksopplysning(saksopplysning)
            Vilkår.ETTERLØNN -> etterlønn.leggTilSaksopplysning(saksopplysning)
            Vilkår.TILTAKDELTAKELSE -> tiltakdeltakelse.leggTilSaksopplysning(saksopplysning)
            else -> {
                throw IllegalArgumentException("Vilkåret ($vilkår) tilhører ikke en ytelseSaksopplysning")
            }
        }
    }
}

private fun ikkeFraSøknad(vilkår: Vilkår, vurderingsperiode: Periode): VilkårDataYtelser {
    return VilkårDataYtelser(
        vilkår = vilkår,
        vurderingsperiode = vurderingsperiode,
        saksopplysningerSaksbehandler = emptyList(),
        saksopplysningerAnnet = emptyList(),
        vurderinger = emptyList(),
    )
}

private fun periodeSpørsmålVilkårFraSøknad(
    vilkår: Vilkår,
    vurderingsperiode: Periode,
    periodeSpm: Søknad.PeriodeSpm,
): VilkårDataYtelser {
    return VilkårDataYtelser(
        vilkår = vilkår, vurderingsperiode, emptyList(),
        listOf(
            lagSaksopplysningFraPeriodespørsmål(
                vilkår = vilkår,
                periodeSpm = periodeSpm,
                periode = vurderingsperiode,
            ),
        ),
        emptyList(),
    )
}

private fun jaNeiSpørsmålVilkårFraSøknad(
    vilkår: Vilkår,
    vurderingsperiode: Periode,
    jaNeiSpm: Søknad.JaNeiSpm,
): VilkårDataYtelser {
    return VilkårDataYtelser(
        vilkår = vilkår, vurderingsperiode, emptyList(),
        listOf(
            lagSaksopplysningFraJaNeiSpørsmål(
                vilkår = vilkår,
                jaNeiSpm = jaNeiSpm,
                periode = vurderingsperiode,
            ),
        ),
        emptyList(),
    )
}

private fun fraOgMedSpørsmålVilkårFraSøknad(
    vilkår: Vilkår,
    vurderingsperiode: Periode,
    fraOgMedSpm: Søknad.FraOgMedDatoSpm,
): VilkårDataYtelser {
    return VilkårDataYtelser(
        vilkår = vilkår, vurderingsperiode, emptyList(),
        listOf(
            lagSaksopplysningFraFraOgMedDatospørsmål(
                vilkår = vilkår,
                fraOgMedDatoSpm = fraOgMedSpm,
                periode = vurderingsperiode,
            ),
        ),
        emptyList(),
    )
}

private fun lagSaksopplysningFraPeriodespørsmål(
    vilkår: Vilkår,
    periodeSpm: Søknad.PeriodeSpm,
    periode: Periode,
): YtelseSaksopplysning {
    return YtelseSaksopplysning(
        periode = Periode(
            fra = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.fra else periode.fra,
            til = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.til else periode.til,
        ),
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        harYtelse = when (periodeSpm) {
            is Søknad.PeriodeSpm.Ja -> true
            is Søknad.PeriodeSpm.Nei -> false
        },
    )
}

private fun lagSaksopplysningFraJaNeiSpørsmål(
    vilkår: Vilkår,
    jaNeiSpm: Søknad.JaNeiSpm,
    periode: Periode,
): YtelseSaksopplysning {
    return YtelseSaksopplysning(
        periode = periode,
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        harYtelse = when (jaNeiSpm) {
            is Søknad.JaNeiSpm.Ja -> true
            is Søknad.JaNeiSpm.Nei -> false
        },
    )
}

private fun lagSaksopplysningFraFraOgMedDatospørsmål(
    vilkår: Vilkår,
    fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
    periode: Periode,
): YtelseSaksopplysning {
    return YtelseSaksopplysning(
        periode = periode,
        vilkår = vilkår,
        kilde = Kilde.SØKNAD,
        detaljer = "",
        harYtelse = when (fraOgMedDatoSpm) {
            is Søknad.FraOgMedDatoSpm.Ja -> true
            is Søknad.FraOgMedDatoSpm.Nei -> false
        },
    )
}
