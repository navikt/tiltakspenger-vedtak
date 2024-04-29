package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning

data class YtelseVilkår(
    val aap: YtelseVilkårData,
    val alderspensjon: YtelseVilkårData,
    val dagpenger: YtelseVilkårData,
    val foreldrepenger: YtelseVilkårData,
    val gjenlevendepensjon: YtelseVilkårData,
    val institusjonsopphold: YtelseVilkårData,
    val introprogrammet: YtelseVilkårData,
    val jobbsjansen: YtelseVilkårData,
    val kvp: YtelseVilkårData,
    val omsorgspenger: YtelseVilkårData,
    val opplæringspenger: YtelseVilkårData,
    val overgangsstønad: YtelseVilkårData,
    val pensjonsinntekt: YtelseVilkårData,
    val pleiepengerNærstående: YtelseVilkårData,
    val pleiepengerSyktBarn: YtelseVilkårData,
    val supplerendestønadalder: YtelseVilkårData,
    val supplerendestønadflyktning: YtelseVilkårData,
    val svangerskapspenger: YtelseVilkårData,
    val sykepenger: YtelseVilkårData,
    val tiltakspenger: YtelseVilkårData,
    val uføretrygd: YtelseVilkårData,
    val etterlønn: YtelseVilkårData,
    val tiltakdeltakelse: YtelseVilkårData,
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

        fun opprettFraSøknad(søknad: Søknad): YtelseVilkår {
            val vurderingsperiode = søknad.vurderingsperiode()
            return YtelseVilkår(
                kvp = periodeSpørsmålVilkårFraSøknad(Vilkår.KVP, vurderingsperiode, søknad.kvp),
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

        // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
        // Denne metoden må fjernes..
        fun tempKompileringsDemp(vurderingsperiode: Periode): YtelseVilkår {
            return YtelseVilkår(
                kvp = ikkeFraSøknad(Vilkår.KVP, vurderingsperiode),
                alderspensjon = ikkeFraSøknad(
                    Vilkår.ALDERSPENSJON,
                    vurderingsperiode,
                ),
                gjenlevendepensjon = ikkeFraSøknad(
                    Vilkår.GJENLEVENDEPENSJON,
                    vurderingsperiode,
                ),
                institusjonsopphold = ikkeFraSøknad(
                    Vilkår.INSTITUSJONSOPPHOLD,
                    vurderingsperiode,
                ),
                introprogrammet = ikkeFraSøknad(
                    Vilkår.INTROPROGRAMMET,
                    vurderingsperiode,
                ),
                jobbsjansen = ikkeFraSøknad(
                    Vilkår.JOBBSJANSEN,
                    vurderingsperiode,
                ),
                pensjonsinntekt = ikkeFraSøknad(
                    Vilkår.PENSJONSINNTEKT,
                    vurderingsperiode,
                ),
                supplerendestønadalder = ikkeFraSøknad(
                    Vilkår.SUPPLERENDESTØNADALDER,
                    vurderingsperiode,
                ),
                supplerendestønadflyktning = ikkeFraSøknad(
                    Vilkår.SUPPLERENDESTØNADFLYKTNING,
                    vurderingsperiode,
                ),
                sykepenger = ikkeFraSøknad(
                    Vilkår.SYKEPENGER,
                    vurderingsperiode,
                ),
                etterlønn = ikkeFraSøknad(
                    Vilkår.ETTERLØNN,
                    vurderingsperiode,
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

    fun leggTilSøknad(søknad: Søknad): YtelseVilkår {
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

    fun vilkårsvurder(): YtelseVilkår {
        return this.copy(
            aap = aap.vilkårsvurder(),
            alderspensjon = alderspensjon.vilkårsvurder(),
            dagpenger = dagpenger.vilkårsvurder(),
            foreldrepenger = foreldrepenger.vilkårsvurder(),
            gjenlevendepensjon = gjenlevendepensjon.vilkårsvurder(),
            institusjonsopphold = institusjonsopphold.vilkårsvurder(),
            introprogrammet = introprogrammet.vilkårsvurder(),
            jobbsjansen = jobbsjansen.vilkårsvurder(),
            kvp = kvp.vilkårsvurder(),
            omsorgspenger = omsorgspenger.vilkårsvurder(),
            opplæringspenger = opplæringspenger.vilkårsvurder(),
            overgangsstønad = overgangsstønad.vilkårsvurder(),
            pensjonsinntekt = pensjonsinntekt.vilkårsvurder(),
            pleiepengerNærstående = pleiepengerNærstående.vilkårsvurder(),
            pleiepengerSyktBarn = pleiepengerSyktBarn.vilkårsvurder(),
            supplerendestønadalder = supplerendestønadalder.vilkårsvurder(),
            supplerendestønadflyktning = supplerendestønadflyktning.vilkårsvurder(),
            svangerskapspenger = svangerskapspenger.vilkårsvurder(),
            sykepenger = sykepenger.vilkårsvurder(),
            tiltakspenger = tiltakspenger.vilkårsvurder(),
            uføretrygd = uføretrygd.vilkårsvurder(),
            etterlønn = etterlønn.vilkårsvurder(),
            tiltakdeltakelse = tiltakdeltakelse.vilkårsvurder()
        )
    }

    fun vurderinger(): List<Vurdering> {
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

    fun leggTilSaksopplysning(saksopplysning: List<YtelseSaksopplysning>): YtelseVilkår {
        val vilkår = saksopplysning.first().vilkår
        return when (vilkår) {
            Vilkår.AAP -> this.copy(aap = aap.leggTilSaksopplysning(saksopplysning))
            Vilkår.ALDERSPENSJON -> this.copy(alderspensjon = alderspensjon.leggTilSaksopplysning(saksopplysning))
            Vilkår.DAGPENGER -> this.copy(dagpenger = dagpenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.FORELDREPENGER -> this.copy(foreldrepenger = foreldrepenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.GJENLEVENDEPENSJON -> this.copy(gjenlevendepensjon = gjenlevendepensjon.leggTilSaksopplysning(saksopplysning))
            Vilkår.INSTITUSJONSOPPHOLD -> this.copy(institusjonsopphold = institusjonsopphold.leggTilSaksopplysning(saksopplysning))
            Vilkår.INTROPROGRAMMET -> this.copy(introprogrammet = introprogrammet.leggTilSaksopplysning(saksopplysning))
            Vilkår.JOBBSJANSEN -> this.copy(jobbsjansen = jobbsjansen.leggTilSaksopplysning(saksopplysning))
            Vilkår.KVP -> this.copy(kvp = kvp.leggTilSaksopplysning(saksopplysning))
            Vilkår.OMSORGSPENGER -> this.copy(omsorgspenger = omsorgspenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.OPPLÆRINGSPENGER -> this.copy(opplæringspenger = opplæringspenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.OVERGANGSSTØNAD -> this.copy(overgangsstønad = overgangsstønad.leggTilSaksopplysning(saksopplysning))
            Vilkår.PENSJONSINNTEKT -> this.copy(pensjonsinntekt = pensjonsinntekt.leggTilSaksopplysning(saksopplysning))
            Vilkår.PLEIEPENGER_NÆRSTÅENDE -> this.copy(pleiepengerNærstående = pleiepengerNærstående.leggTilSaksopplysning(saksopplysning))
            Vilkår.PLEIEPENGER_SYKT_BARN -> this.copy(pleiepengerSyktBarn = pleiepengerSyktBarn.leggTilSaksopplysning(saksopplysning))
            Vilkår.SUPPLERENDESTØNADALDER -> this.copy(supplerendestønadalder = supplerendestønadalder.leggTilSaksopplysning(saksopplysning))
            Vilkår.SUPPLERENDESTØNADFLYKTNING -> this.copy(supplerendestønadflyktning = supplerendestønadflyktning.leggTilSaksopplysning(saksopplysning))
            Vilkår.SVANGERSKAPSPENGER -> this.copy(svangerskapspenger = svangerskapspenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.SYKEPENGER -> this.copy(sykepenger = sykepenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.TILTAKSPENGER -> this.copy(tiltakspenger = tiltakspenger.leggTilSaksopplysning(saksopplysning))
            Vilkår.UFØRETRYGD -> this.copy(uføretrygd = uføretrygd.leggTilSaksopplysning(saksopplysning))
            Vilkår.ETTERLØNN -> this.copy(etterlønn = etterlønn.leggTilSaksopplysning(saksopplysning))
            Vilkår.TILTAKDELTAKELSE -> this.copy(tiltakdeltakelse = tiltakdeltakelse.leggTilSaksopplysning(saksopplysning))
            else -> {
                throw IllegalArgumentException("Vilkåret ($vilkår) tilhører ikke en ytelseSaksopplysning")
            }
        }
    }
}

private fun ikkeFraSøknad(vilkår: Vilkår, vurderingsperiode: Periode): YtelseVilkårData {
    return YtelseVilkårData(
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
): YtelseVilkårData {
    return YtelseVilkårData(
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
): YtelseVilkårData {
    return YtelseVilkårData(
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
): YtelseVilkårData {
    return YtelseVilkårData(
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
