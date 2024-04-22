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
        fun opprettFraSøknad(søknad: Søknad): VilkårYtelser {
            return VilkårYtelser(
                kvp = vilkårFraSøknad(Vilkår.KVP, søknad.vurderingsperiode(), søknad.kvp),
                alderspensjon = vilkårFraSøknad(Vilkår.ALDERSPENSJON, søknad.vurderingsperiode(), søknad.alderspensjon),
                gjenlevendepensjon = vilkårFraSøknad(Vilkår.GJENLEVENDEPENSJON, søknad.vurderingsperiode(), søknad.gjenlevendepensjon),
                institusjonsopphold = vilkårFraSøknad(Vilkår.INSTITUSJONSOPPHOLD, søknad.vurderingsperiode(), søknad.institusjon),
                introprogrammet = vilkårFraSøknad(Vilkår.INTROPROGRAMMET, søknad.vurderingsperiode(), søknad.intro),
                jobbsjansen = vilkårFraSøknad(Vilkår.JOBBSJANSEN, søknad.vurderingsperiode(), søknad.jobbsjansen),
                pensjonsinntekt = vilkårFraSøknad(Vilkår.PENSJONSINNTEKT, søknad.vurderingsperiode(), søknad.trygdOgPensjon),
                supplerendestønadalder = vilkårFraSøknad(Vilkår.SUPPLERENDESTØNADALDER, søknad.vurderingsperiode(), søknad.supplerendeStønadAlder),
                supplerendestønadflyktning = vilkårFraSøknad(Vilkår.SUPPLERENDESTØNADFLYKTNING, søknad.vurderingsperiode(), søknad.supplerendeStønadFlyktning),
                sykepenger = vilkårFraSøknad(Vilkår.SYKEPENGER, søknad.vurderingsperiode(), søknad.sykepenger),
                etterlønn = vilkårFraSøknad(Vilkår.ETTERLØNN, søknad.vurderingsperiode(), søknad.etterlønn),
                aap = ikkeFraSøknad(Vilkår.AAP, søknad.vurderingsperiode()),
                dagpenger = ikkeFraSøknad(Vilkår.DAGPENGER, søknad.vurderingsperiode()),
                foreldrepenger = ikkeFraSøknad(Vilkår.FORELDREPENGER, søknad.vurderingsperiode()),
                omsorgspenger = ikkeFraSøknad(Vilkår.OMSORGSPENGER, søknad.vurderingsperiode()),
                opplæringspenger = ikkeFraSøknad(Vilkår.OPPLÆRINGSPENGER, søknad.vurderingsperiode()),
                overgangsstønad = ikkeFraSøknad(Vilkår.OVERGANGSSTØNAD, søknad.vurderingsperiode()),
                pleiepengerNærstående = ikkeFraSøknad(Vilkår.PLEIEPENGER_NÆRSTÅENDE, søknad.vurderingsperiode()),
                pleiepengerSyktBarn = ikkeFraSøknad(Vilkår.PLEIEPENGER_SYKT_BARN, søknad.vurderingsperiode()),
                svangerskapspenger = ikkeFraSøknad(Vilkår.SVANGERSKAPSPENGER, søknad.vurderingsperiode()),
                tiltakspenger = ikkeFraSøknad(Vilkår.TILTAKSPENGER, søknad.vurderingsperiode()),
                uføretrygd = ikkeFraSøknad(Vilkår.UFØRETRYGD, søknad.vurderingsperiode()),
                tiltakdeltakelse = ikkeFraSøknad(Vilkår.TILTAKDELTAKELSE, søknad.vurderingsperiode()),
            )
        }
    }

    fun leggTilSøknad(søknad: Søknad): VilkårYtelser {
        TODO()
    }

    fun vilkårsvurder(): List<Vurdering> {
        return aap.vilkårsvurder().vurderinger +
            kvp.vilkårsvurder().vurderinger
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
private fun vilkårFraSøknad(vilkår: Vilkår, vurderingsperiode: Periode, periodeSpm: Søknad.PeriodeSpm): VilkårDataYtelser {
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

private fun vilkårFraSøknad(vilkår: Vilkår, vurderingsperiode: Periode, jaNeiSpm: Søknad.JaNeiSpm): VilkårDataYtelser {
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

private fun vilkårFraSøknad(vilkår: Vilkår, vurderingsperiode: Periode, fraOgMedSpm: Søknad.FraOgMedDatoSpm): VilkårDataYtelser {
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
