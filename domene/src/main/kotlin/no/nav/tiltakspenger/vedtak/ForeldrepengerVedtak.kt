package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import java.math.BigDecimal
import java.time.LocalDateTime

data class ForeldrepengerVedtak(
    val id: ForeldrepengerVedtakId,
    val version: String,
    val aktør: String,
    val vedtattTidspunkt: LocalDateTime,
    val ytelse: Ytelser,
    val saksnummer: String?,
    val vedtakReferanse: String,
    val ytelseStatus: Status,
    val kildesystem: Kildesystem,
    val periode: Periode,
    val tilleggsopplysninger: String?,
    val anvist: List<ForeldrepengerAnvisning>,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet

    data class ForeldrepengerAnvisning(
        val periode: Periode,
        val beløp: BigDecimal?,
        val dagsats: BigDecimal?,
        val utbetalingsgrad: BigDecimal?,
    )

    enum class Ytelser {
        /** Folketrygdloven K9 ytelser.  */
        PLEIEPENGER_SYKT_BARN,
        PLEIEPENGER_NÆRSTÅENDE,
        OMSORGSPENGER,
        OPPLÆRINGSPENGER,

        /** Folketrygdloven K14 ytelser.  */
        ENGANGSTØNAD,
        FORELDREPENGER,
        SVANGERSKAPSPENGER,

        /** Midlertidig ytelse for Selvstendig næringsdrivende og Frilansere (Anmodning 10).  */
        FRISINN
    }

    enum class Status {
        UNDER_BEHANDLING, LØPENDE, AVSLUTTET, UKJENT
    }

    enum class Kildesystem {
        FPSAK,
        K9SAK
    }
}
