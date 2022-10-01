@file:Suppress("LongParameterList")
package no.nav.tiltakspenger.vedtak.testcommon

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakStatus
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak
import java.time.LocalDate
import java.time.LocalDateTime

fun tomYtelsesak(
    fomGyldighetsperiode: LocalDateTime = 1.januarDateTime(2022),
    tomGyldighetsperiode: LocalDateTime? = null,
    datoKravMottatt: LocalDate? = null,
    dataKravMottatt: String? = null,
    fagsystemSakId: Int? = null,
    status: YtelseSakStatus? = null,
    ytelsestype: YtelseSakYtelsetype? = null,
    vedtak: List<YtelseVedtak> = emptyList(),
    antallDagerIgjen: Int? = null,
    antallUkerIgjen: Int? = null,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
) : YtelseSak {
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

fun nyYtelsesak(
    fomGyldighetsperiode: LocalDateTime = 1.januarDateTime(2022),
    tomGyldighetsperiode: LocalDateTime? = 31.januarDateTime(2022),
    datoKravMottatt: LocalDate? = 1.januar(2022),
    dataKravMottatt: String? = "data",
    fagsystemSakId: Int? = 1,
    status: YtelseSakStatus? = YtelseSakStatus.AKTIV,
    ytelsestype: YtelseSakYtelsetype? = YtelseSakYtelsetype.ANNET,
    vedtak: List<YtelseVedtak> = emptyList(),
    antallDagerIgjen: Int? = 7,
    antallUkerIgjen: Int? = 1,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
) : YtelseSak {
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
