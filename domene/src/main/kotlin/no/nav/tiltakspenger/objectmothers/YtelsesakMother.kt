@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakStatus
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse
import java.time.LocalDate
import java.time.LocalDateTime

fun ytelseSakAAP(): List<YtelseSak> {
    return listOf(ytelseSak(ytelsestype = YtelseSakYtelsetype.AA))
}

fun ytelseSak(
    fomGyldighetsperiode: LocalDateTime = 1.januarDateTime(2022),
    tomGyldighetsperiode: LocalDateTime? = 31.januarDateTime(2022),
    datoKravMottatt: LocalDate? = 1.januar(2022),
    dataKravMottatt: String? = "data",
    fagsystemSakId: String? = "123",
    status: YtelseSakStatus? = YtelseSakStatus.AKTIV,
    ytelsestype: YtelseSakYtelsetype? = YtelseSakYtelsetype.ANNET,
    vedtak: List<YtelseVedtak> = emptyList(),
    antallDagerIgjen: Int? = 7,
    antallUkerIgjen: Int? = 1,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): YtelseSak {
    return YtelseSak(
        fomGyldighetsperiode = fomGyldighetsperiode,
        tomGyldighetsperiode = tomGyldighetsperiode,
        datoKravMottatt = datoKravMottatt,
        dataKravMottatt = dataKravMottatt,
        fagsystemSakId = fagsystemSakId,
        status = status,
        ytelsestype = ytelsestype,
        vedtak = vedtak,
        antallDagerIgjen = antallDagerIgjen,
        antallUkerIgjen = antallUkerIgjen,
        tidsstempelHosOss = tidsstempelHosOss,
    )
}

fun tomYtelsesak(
    fomGyldighetsperiode: LocalDateTime = 1.januarDateTime(2022),
    tomGyldighetsperiode: LocalDateTime? = null,
    datoKravMottatt: LocalDate? = null,
    dataKravMottatt: String? = null,
    fagsystemSakId: String? = null,
    status: YtelseSakStatus? = null,
    ytelsestype: YtelseSakYtelsetype? = null,
    vedtak: List<YtelseVedtak> = emptyList(),
    antallDagerIgjen: Int? = null,
    antallUkerIgjen: Int? = null,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): YtelseSak {
    return YtelseSak(
        fomGyldighetsperiode = fomGyldighetsperiode,
        tomGyldighetsperiode = tomGyldighetsperiode,
        datoKravMottatt = datoKravMottatt,
        dataKravMottatt = dataKravMottatt,
        fagsystemSakId = fagsystemSakId,
        status = status,
        ytelsestype = ytelsestype,
        vedtak = vedtak,
        antallDagerIgjen = antallDagerIgjen,
        antallUkerIgjen = antallUkerIgjen,
        tidsstempelHosOss = tidsstempelHosOss,
    )
}

fun ytelseVedtak(
    beslutningsDato: LocalDate? = 1.januar(2022),
    periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse = YtelseVedtakPeriodeTypeForYtelse.F,
    vedtaksperiodeFom: LocalDate? = 1.januar(2022),
    vedtaksperiodeTom: LocalDate? = 31.januar(2022),
    vedtaksType: YtelseVedtak.YtelseVedtakVedtakstype = YtelseVedtak.YtelseVedtakVedtakstype.AA115,
    status: YtelseVedtak.YtelseVedtakStatus = YtelseVedtak.YtelseVedtakStatus.GODKJ
): YtelseVedtak {
    return YtelseVedtak(
        beslutningsDato = beslutningsDato,
        periodetypeForYtelse = periodetypeForYtelse,
        vedtaksperiodeFom = vedtaksperiodeFom,
        vedtaksperiodeTom = vedtaksperiodeTom,
        vedtaksType = vedtaksType,
        status = status,
    )
}

fun tomYtelsevedtak(
    beslutningsDato: LocalDate? = null,
    periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse? = null,
    vedtaksperiodeFom: LocalDate? = null,
    vedtaksperiodeTom: LocalDate? = null,
    vedtaksType: YtelseVedtak.YtelseVedtakVedtakstype? = null,
    status: YtelseVedtak.YtelseVedtakStatus? = null,
): YtelseVedtak {
    return YtelseVedtak(
        beslutningsDato = beslutningsDato,
        periodetypeForYtelse = periodetypeForYtelse,
        vedtaksperiodeFom = vedtaksperiodeFom,
        vedtaksperiodeTom = vedtaksperiodeTom,
        vedtaksType = vedtaksType,
        status = status,
    )
}
