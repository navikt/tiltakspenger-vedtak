@file:Suppress("LongParameterList")
package no.nav.tiltakspenger.vedtak.testcommon

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak.YtelseVedtakStatus
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype
import java.time.LocalDate

fun tomYtelsevedtak(
    beslutningsDato: LocalDate? = null,
    periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse? = null,
    vedtaksperiodeFom: LocalDate? = null,
    vedtaksperiodeTom: LocalDate? = null,
    vedtaksType: YtelseVedtakVedtakstype? = null,
    status: YtelseVedtakStatus? = null,
) : YtelseVedtak {
    return YtelseVedtak(
        beslutningsDato = beslutningsDato,
        periodetypeForYtelse = periodetypeForYtelse,
        vedtaksperiodeFom = vedtaksperiodeFom,
        vedtaksperiodeTom = vedtaksperiodeTom,
        vedtaksType = vedtaksType,
        status = status,
    )
}

fun ytelsevedtakGodkjent(
    beslutningsDato: LocalDate? = 1.januar(2022),
    periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse? = YtelseVedtakPeriodeTypeForYtelse.F,
    vedtaksperiodeFom: LocalDate? = 1.januar(2022),
    vedtaksperiodeTom: LocalDate? = 31.januar(2022),
    vedtaksType: YtelseVedtakVedtakstype? = YtelseVedtakVedtakstype.AA115,
    status: YtelseVedtakStatus? = YtelseVedtakStatus.GODKJ,
) : YtelseVedtak {
    return tomYtelsevedtak(
        beslutningsDato = beslutningsDato,
        periodetypeForYtelse = periodetypeForYtelse,
        vedtaksperiodeFom = vedtaksperiodeFom,
        vedtaksperiodeTom = vedtaksperiodeTom,
        vedtaksType = vedtaksType,
        status = status,
    )
}
