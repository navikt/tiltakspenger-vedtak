package no.nav.tiltakspenger.innsending

import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Kilde
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

    enum class Ytelser(val rettTilTiltakspenger: Boolean, val kilde: Kilde) {
        /** Folketrygdloven K9 ytelser.  */
        PLEIEPENGER_SYKT_BARN(true, Kilde.K9SAK),
        PLEIEPENGER_NÆRSTÅENDE(true, Kilde.K9SAK),
        OMSORGSPENGER(true, Kilde.K9SAK),
        OPPLÆRINGSPENGER(true, Kilde.K9SAK),

        /** Folketrygdloven K14 ytelser.  */
        ENGANGSTØNAD(false, Kilde.FPSAK),
        FORELDREPENGER(true, Kilde.FPSAK),
        SVANGERSKAPSPENGER(true, Kilde.FPSAK),

        /** Midlertidig ytelse for Selvstendig næringsdrivende og Frilansere (Anmodning 10).  */
        FRISINN(false, Kilde.FPSAK),
    }

    enum class Status {
        UNDER_BEHANDLING, LØPENDE, AVSLUTTET, UKJENT
    }

    enum class Kildesystem {
        FPSAK,
        K9SAK,
    }
}
