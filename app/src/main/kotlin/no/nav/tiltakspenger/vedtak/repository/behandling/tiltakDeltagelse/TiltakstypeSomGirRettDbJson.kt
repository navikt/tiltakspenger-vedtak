package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett

/**
 * @see [TiltakstypeSomGirRett]
 */
private enum class TiltakstypeSomGirRettDbJson {
    ARBEIDSFORBEREDENDE_TRENING,
    ARBEIDSRETTET_REHABILITERING,
    ARBEIDSTRENING,
    AVKLARING,
    DIGITAL_JOBBKLUBB,
    ENKELTPLASS_AMO,
    ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG,
    FORSØK_OPPLÆRING_LENGRE_VARIGHET,
    GRUPPE_AMO,
    GRUPPE_VGS_OG_HØYERE_YRKESFAG,
    HØYERE_UTDANNING,
    INDIVIDUELL_JOBBSTØTTE,
    INDIVIDUELL_KARRIERESTØTTE_UNG,
    JOBBKLUBB,
    OPPFØLGING,
    UTVIDET_OPPFØLGING_I_NAV,
    UTVIDET_OPPFØLGING_I_OPPLÆRING,
}

internal fun TiltakstypeSomGirRett.toDb(): String =
    when (this) {
        TiltakstypeSomGirRett.ARBEIDSFORBEREDENDE_TRENING -> TiltakstypeSomGirRettDbJson.ARBEIDSFORBEREDENDE_TRENING.name
        TiltakstypeSomGirRett.ARBEIDSRETTET_REHABILITERING -> TiltakstypeSomGirRettDbJson.ARBEIDSRETTET_REHABILITERING.name
        TiltakstypeSomGirRett.ARBEIDSTRENING -> TiltakstypeSomGirRettDbJson.ARBEIDSTRENING.name
        TiltakstypeSomGirRett.AVKLARING -> TiltakstypeSomGirRettDbJson.AVKLARING.name
        TiltakstypeSomGirRett.DIGITAL_JOBBKLUBB -> TiltakstypeSomGirRettDbJson.DIGITAL_JOBBKLUBB.name
        TiltakstypeSomGirRett.ENKELTPLASS_AMO -> TiltakstypeSomGirRettDbJson.ENKELTPLASS_AMO.name
        TiltakstypeSomGirRett.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG -> TiltakstypeSomGirRettDbJson.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG.name
        TiltakstypeSomGirRett.FORSØK_OPPLÆRING_LENGRE_VARIGHET -> TiltakstypeSomGirRettDbJson.FORSØK_OPPLÆRING_LENGRE_VARIGHET.name
        TiltakstypeSomGirRett.GRUPPE_AMO -> TiltakstypeSomGirRettDbJson.GRUPPE_AMO.name
        TiltakstypeSomGirRett.GRUPPE_VGS_OG_HØYERE_YRKESFAG -> TiltakstypeSomGirRettDbJson.GRUPPE_VGS_OG_HØYERE_YRKESFAG.name
        TiltakstypeSomGirRett.HØYERE_UTDANNING -> TiltakstypeSomGirRettDbJson.HØYERE_UTDANNING.name
        TiltakstypeSomGirRett.INDIVIDUELL_JOBBSTØTTE -> TiltakstypeSomGirRettDbJson.INDIVIDUELL_JOBBSTØTTE.name
        TiltakstypeSomGirRett.INDIVIDUELL_KARRIERESTØTTE_UNG -> TiltakstypeSomGirRettDbJson.INDIVIDUELL_KARRIERESTØTTE_UNG.name
        TiltakstypeSomGirRett.JOBBKLUBB -> TiltakstypeSomGirRettDbJson.JOBBKLUBB.name
        TiltakstypeSomGirRett.OPPFØLGING -> TiltakstypeSomGirRettDbJson.OPPFØLGING.name
        TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_NAV -> TiltakstypeSomGirRettDbJson.UTVIDET_OPPFØLGING_I_NAV.name
        TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_OPPLÆRING -> TiltakstypeSomGirRettDbJson.UTVIDET_OPPFØLGING_I_OPPLÆRING.name
    }

internal fun String.toTiltakstypeSomGirRett(): TiltakstypeSomGirRett =
    when (TiltakstypeSomGirRettDbJson.valueOf(this)) {
        TiltakstypeSomGirRettDbJson.ARBEIDSFORBEREDENDE_TRENING -> TiltakstypeSomGirRett.ARBEIDSFORBEREDENDE_TRENING
        TiltakstypeSomGirRettDbJson.ARBEIDSRETTET_REHABILITERING -> TiltakstypeSomGirRett.ARBEIDSRETTET_REHABILITERING
        TiltakstypeSomGirRettDbJson.ARBEIDSTRENING -> TiltakstypeSomGirRett.ARBEIDSTRENING
        TiltakstypeSomGirRettDbJson.AVKLARING -> TiltakstypeSomGirRett.AVKLARING
        TiltakstypeSomGirRettDbJson.DIGITAL_JOBBKLUBB -> TiltakstypeSomGirRett.DIGITAL_JOBBKLUBB
        TiltakstypeSomGirRettDbJson.ENKELTPLASS_AMO -> TiltakstypeSomGirRett.ENKELTPLASS_AMO
        TiltakstypeSomGirRettDbJson.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG -> TiltakstypeSomGirRett.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG
        TiltakstypeSomGirRettDbJson.FORSØK_OPPLÆRING_LENGRE_VARIGHET -> TiltakstypeSomGirRett.FORSØK_OPPLÆRING_LENGRE_VARIGHET
        TiltakstypeSomGirRettDbJson.GRUPPE_AMO -> TiltakstypeSomGirRett.GRUPPE_AMO
        TiltakstypeSomGirRettDbJson.GRUPPE_VGS_OG_HØYERE_YRKESFAG -> TiltakstypeSomGirRett.GRUPPE_VGS_OG_HØYERE_YRKESFAG
        TiltakstypeSomGirRettDbJson.HØYERE_UTDANNING -> TiltakstypeSomGirRett.HØYERE_UTDANNING
        TiltakstypeSomGirRettDbJson.INDIVIDUELL_JOBBSTØTTE -> TiltakstypeSomGirRett.INDIVIDUELL_JOBBSTØTTE
        TiltakstypeSomGirRettDbJson.INDIVIDUELL_KARRIERESTØTTE_UNG -> TiltakstypeSomGirRett.INDIVIDUELL_KARRIERESTØTTE_UNG
        TiltakstypeSomGirRettDbJson.JOBBKLUBB -> TiltakstypeSomGirRett.JOBBKLUBB
        TiltakstypeSomGirRettDbJson.OPPFØLGING -> TiltakstypeSomGirRett.OPPFØLGING
        TiltakstypeSomGirRettDbJson.UTVIDET_OPPFØLGING_I_NAV -> TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_NAV
        TiltakstypeSomGirRettDbJson.UTVIDET_OPPFØLGING_I_OPPLÆRING -> TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_OPPLÆRING
    }
