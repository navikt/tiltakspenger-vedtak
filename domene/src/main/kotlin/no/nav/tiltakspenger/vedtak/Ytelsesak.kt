package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

data class YtelseSak(
    val fomGyldighetsperiode: LocalDateTime,
    val tomGyldighetsperiode: LocalDateTime,
    val datoKravMottatt: LocalDate?,
    val dataKravMottatt: String? = null,
    val fagsystemSakId: Int? = null,
    val status: String? = null,
    val ytelsestype: String? = null,
    val vedtak: List<YtelseVedtak> = emptyList(),
    val antallDagerIgjen: Int? = null,
    val antallUkerIgjen: Int? = null,
) {

    data class YtelseVedtak(
        val beslutningsDato: LocalDate? = null,
        val periodetypeForYtelse: String? = null,
        val vedtaksperiodeFom: LocalDate? = null,
        val vedtaksperiodeTom: LocalDate? = null,
        val vedtaksType: String? = null,
        val status: String? = null,
    )
}