@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.innsending.domene.YtelseSak
import java.time.LocalDate
import java.time.LocalDateTime

interface YtelsesakMother {
    fun ytelseSakAAP(
        fom: LocalDateTime = 1.januarDateTime(2022),
        tom: LocalDateTime = 31.januarDateTime(2022),
    ): List<YtelseSak> {
        return listOf(
            ytelseSak(
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA,
                fomGyldighetsperiode = fom,
                tomGyldighetsperiode = tom,
            ),
        )
    }

    fun ytelseSak(
        fomGyldighetsperiode: LocalDateTime = 1.januarDateTime(2022),
        tomGyldighetsperiode: LocalDateTime? = 31.januarDateTime(2022),
        datoKravMottatt: LocalDate? = 1.januar(2022),
        dataKravMottatt: String? = "data",
        fagsystemSakId: String? = "123",
        status: YtelseSak.YtelseSakStatus? = YtelseSak.YtelseSakStatus.AKTIV,
        ytelsestype: YtelseSak.YtelseSakYtelsetype? = YtelseSak.YtelseSakYtelsetype.ANNET,
        vedtak: List<YtelseSak.YtelseVedtak> = emptyList(),
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
        status: YtelseSak.YtelseSakStatus? = null,
        ytelsestype: YtelseSak.YtelseSakYtelsetype? = null,
        vedtak: List<YtelseSak.YtelseVedtak> = emptyList(),
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
        periodetypeForYtelse: YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse = YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse.F,
        vedtaksperiodeFom: LocalDate? = 1.januar(2022),
        vedtaksperiodeTom: LocalDate? = 31.januar(2022),
        vedtaksType: YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype = YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype.AA115,
        status: YtelseSak.YtelseVedtak.YtelseVedtakStatus = YtelseSak.YtelseVedtak.YtelseVedtakStatus.GODKJ,
    ): YtelseSak.YtelseVedtak {
        return YtelseSak.YtelseVedtak(
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
        periodetypeForYtelse: YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse? = null,
        vedtaksperiodeFom: LocalDate? = null,
        vedtaksperiodeTom: LocalDate? = null,
        vedtaksType: YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype? = null,
        status: YtelseSak.YtelseVedtak.YtelseVedtakStatus? = null,
    ): YtelseSak.YtelseVedtak {
        return YtelseSak.YtelseVedtak(
            beslutningsDato = beslutningsDato,
            periodetypeForYtelse = periodetypeForYtelse,
            vedtaksperiodeFom = vedtaksperiodeFom,
            vedtaksperiodeTom = vedtaksperiodeTom,
            vedtaksType = vedtaksType,
            status = status,
        )
    }
}
