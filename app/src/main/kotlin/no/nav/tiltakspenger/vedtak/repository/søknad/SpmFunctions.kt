package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.db.booleanOrNull

private const val JA = "JA"
private const val NEI = "NEI"
private const val IKKE_RELEVANT = "IKKE_RELEVANT"
private const val IKKE_MED_I_SOKNADEN = "IKKE_MED_I_SOKNADEN"
private const val FEILAKTIG_BESVART = "FEILAKTIG_BESVART"
private const val IKKE_BESVART = "IKKE_BESVART"

private const val JA_SUFFIX = "_ja"
private const val FOM_SUFFIX = "_fom"
private const val TOM_SUFFIX = "_tom"
private const val TYPE_SUFFIX = "_type"

fun Row.periodeSpm(navn: String): Søknad.PeriodeSpm {
    val type = string(navn + TYPE_SUFFIX)
    val janei = booleanOrNull(navn + JA_SUFFIX)
    val fom = localDateOrNull(navn + FOM_SUFFIX)
    val tom = localDateOrNull(navn + TOM_SUFFIX)
    // TODO: Dobbeltsjekke at det er lovlige kombinasjoner som leses opp?
    return when (type) {
        JA -> Søknad.PeriodeSpm.Ja(Periode(fom!!, tom!!))
        NEI -> Søknad.PeriodeSpm.Nei
        IKKE_RELEVANT -> Søknad.PeriodeSpm.IkkeRelevant
        IKKE_MED_I_SOKNADEN -> Søknad.PeriodeSpm.IkkeMedISøknaden
        FEILAKTIG_BESVART -> Søknad.PeriodeSpm.FeilaktigBesvart(janei, fom, tom)
        IKKE_BESVART -> Søknad.PeriodeSpm.IkkeBesvart
        else -> throw IllegalArgumentException("Ugyldig type")
    }
}

fun Row.fraOgMedDatoSpm(navn: String): Søknad.FraOgMedDatoSpm {
    val type = string(navn + TYPE_SUFFIX)
    val janei = booleanOrNull(navn + JA_SUFFIX)
    val fom = localDateOrNull(navn + FOM_SUFFIX)
    // TODO: Dobbeltsjekke at det er lovlige kombinasjoner som leses opp?
    return when (type) {
        JA -> Søknad.FraOgMedDatoSpm.Ja(fom!!)
        NEI -> Søknad.FraOgMedDatoSpm.Nei
        IKKE_RELEVANT -> Søknad.FraOgMedDatoSpm.IkkeRelevant
        IKKE_MED_I_SOKNADEN -> Søknad.FraOgMedDatoSpm.IkkeMedISøknaden
        FEILAKTIG_BESVART -> Søknad.FraOgMedDatoSpm.FeilaktigBesvart(janei, fom)
        IKKE_BESVART -> Søknad.FraOgMedDatoSpm.IkkeBesvart
        else -> throw IllegalArgumentException("Ugyldig type")
    }
}

fun Row.jaNeiSpm(navn: String): Søknad.JaNeiSpm {
    return when (string(navn + TYPE_SUFFIX)) {
        JA -> Søknad.JaNeiSpm.Ja
        NEI -> Søknad.JaNeiSpm.Nei
        IKKE_RELEVANT -> Søknad.JaNeiSpm.IkkeRelevant
        IKKE_MED_I_SOKNADEN -> Søknad.JaNeiSpm.IkkeMedISøknaden
        IKKE_BESVART -> Søknad.JaNeiSpm.IkkeBesvart
        else -> throw IllegalArgumentException("Ugyldig type")
    }
}

fun Map<String, Søknad.PeriodeSpm>.toPeriodeSpmParams(): Map<String, Any?> =
    this.flatMap { (k, v) ->
        listOf(
            k + TYPE_SUFFIX to lagrePeriodeSpmType(v),
            k + JA_SUFFIX to lagrePeriodeSpmJa(v),
            k + FOM_SUFFIX to lagrePeriodeSpmFra(v),
            k + TOM_SUFFIX to lagrePeriodeSpmTil(v),
        )
    }.associate {
        it.first to it.second as Any?
    }

fun Map<String, Søknad.FraOgMedDatoSpm>.toFraOgMedDatoSpmParams(): Map<String, Any?> =
    this.flatMap { (k, v) ->
        listOf(
            k + TYPE_SUFFIX to lagreFraOgMedDatoSpmType(v),
            k + JA_SUFFIX to lagreFraOgMedDatoSpmJa(v),
            k + FOM_SUFFIX to lagreFraOgMedDatoSpmFra(v),
        )
    }.associate {
        it.first to it.second as Any?
    }

fun Map<String, Søknad.JaNeiSpm>.toJaNeiSpmParams(): Map<String, Any?> =
    this.flatMap { (k, v) ->
        listOf(
            k + TYPE_SUFFIX to lagreJaNeiSpmType(v),
        )
    }.associate {
        it.first to it.second as Any?
    }

fun lagrePeriodeSpmType(periodeSpm: Søknad.PeriodeSpm) =
    when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> JA
        is Søknad.PeriodeSpm.Nei -> NEI
        is Søknad.PeriodeSpm.IkkeRelevant -> IKKE_RELEVANT
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> IKKE_MED_I_SOKNADEN
        is Søknad.PeriodeSpm.FeilaktigBesvart -> FEILAKTIG_BESVART
        is Søknad.PeriodeSpm.IkkeBesvart -> IKKE_BESVART
    }

fun lagrePeriodeSpmJa(periodeSpm: Søknad.PeriodeSpm) =
    when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> true
        is Søknad.PeriodeSpm.Nei -> false
        is Søknad.PeriodeSpm.IkkeRelevant -> null
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> null
        is Søknad.PeriodeSpm.FeilaktigBesvart -> periodeSpm.svartJa
        is Søknad.PeriodeSpm.IkkeBesvart -> null
    }

fun lagrePeriodeSpmFra(periodeSpm: Søknad.PeriodeSpm) =
    when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> periodeSpm.periode.fra
        is Søknad.PeriodeSpm.Nei -> null
        is Søknad.PeriodeSpm.IkkeRelevant -> null
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> null
        is Søknad.PeriodeSpm.FeilaktigBesvart -> periodeSpm.fom
        is Søknad.PeriodeSpm.IkkeBesvart -> null
    }

fun lagrePeriodeSpmTil(periodeSpm: Søknad.PeriodeSpm) =
    when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> periodeSpm.periode.til
        is Søknad.PeriodeSpm.Nei -> null
        is Søknad.PeriodeSpm.IkkeRelevant -> null
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> null
        is Søknad.PeriodeSpm.FeilaktigBesvart -> periodeSpm.tom
        is Søknad.PeriodeSpm.IkkeBesvart -> null
    }

fun lagreFraOgMedDatoSpmType(fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm) =
    when (fraOgMedDatoSpm) {
        is Søknad.FraOgMedDatoSpm.Ja -> JA
        is Søknad.FraOgMedDatoSpm.Nei -> NEI
        is Søknad.FraOgMedDatoSpm.IkkeRelevant -> IKKE_RELEVANT
        is Søknad.FraOgMedDatoSpm.IkkeMedISøknaden -> IKKE_MED_I_SOKNADEN
        is Søknad.FraOgMedDatoSpm.FeilaktigBesvart -> FEILAKTIG_BESVART
        is Søknad.FraOgMedDatoSpm.IkkeBesvart -> IKKE_BESVART
    }

fun lagreFraOgMedDatoSpmJa(fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm) =
    when (fraOgMedDatoSpm) {
        is Søknad.FraOgMedDatoSpm.Ja -> true
        is Søknad.FraOgMedDatoSpm.Nei -> false
        is Søknad.FraOgMedDatoSpm.IkkeRelevant -> null
        is Søknad.FraOgMedDatoSpm.IkkeMedISøknaden -> null
        is Søknad.FraOgMedDatoSpm.FeilaktigBesvart -> fraOgMedDatoSpm.svartJa
        is Søknad.FraOgMedDatoSpm.IkkeBesvart -> null
    }

fun lagreFraOgMedDatoSpmFra(fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm) =
    when (fraOgMedDatoSpm) {
        is Søknad.FraOgMedDatoSpm.Ja -> fraOgMedDatoSpm.fra
        is Søknad.FraOgMedDatoSpm.Nei -> null
        is Søknad.FraOgMedDatoSpm.IkkeRelevant -> null
        is Søknad.FraOgMedDatoSpm.IkkeMedISøknaden -> null
        is Søknad.FraOgMedDatoSpm.FeilaktigBesvart -> fraOgMedDatoSpm.fom
        is Søknad.FraOgMedDatoSpm.IkkeBesvart -> null
    }

fun lagreJaNeiSpmType(jaNeiSpm: Søknad.JaNeiSpm): String =
    when (jaNeiSpm) {
        is Søknad.JaNeiSpm.Ja -> JA
        is Søknad.JaNeiSpm.Nei -> NEI
        is Søknad.JaNeiSpm.IkkeRelevant -> IKKE_RELEVANT
        is Søknad.JaNeiSpm.IkkeMedISøknaden -> IKKE_MED_I_SOKNADEN
        is Søknad.JaNeiSpm.IkkeBesvart -> IKKE_BESVART
    }

fun lagreJaNeiSpmJa(jaNeiSpm: Søknad.JaNeiSpm) =
    when (jaNeiSpm) {
        is Søknad.JaNeiSpm.Ja -> true
        is Søknad.JaNeiSpm.Nei -> false
        is Søknad.JaNeiSpm.IkkeRelevant -> null
        is Søknad.JaNeiSpm.IkkeMedISøknaden -> null
        is Søknad.JaNeiSpm.IkkeBesvart -> null
    }
