package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import java.math.BigDecimal
import java.time.LocalDateTime

interface ForeldrepengerMother {
    fun foreldrepengerVedtak(
        id: ForeldrepengerVedtakId = ForeldrepengerVedtakId.random(),
        version: String = "v1",
        aktør: String = "aktørId",
        vedtattTidspunkt: LocalDateTime = LocalDateTime.of(2022, 1, 1, 12, 0, 0, 0),
        ytelse: ForeldrepengerVedtak.Ytelser = ForeldrepengerVedtak.Ytelser.FORELDREPENGER,
        saksnummer: String? = "saksnr",
        vedtakRef: String = "vedtakRef",
        status: ForeldrepengerVedtak.Status = ForeldrepengerVedtak.Status.LØPENDE,
        kildesystem: ForeldrepengerVedtak.Kildesystem = ForeldrepengerVedtak.Kildesystem.FPSAK,
        periode: Periode = Periode(fraOgMed = 1.januar(2022), tilOgMed = 31.januar(2022)),
        tilleggsopplysninger: String = "Tillegg",
        anvist: List<ForeldrepengerVedtak.ForeldrepengerAnvisning> = listOf(anvist(periode = periode)),
        innhentet: LocalDateTime = LocalDateTime.of(2022, 1, 1, 12, 0, 0, 0),
    ): ForeldrepengerVedtak =
        ForeldrepengerVedtak(
            id = id,
            version = version,
            aktør = aktør,
            vedtattTidspunkt = vedtattTidspunkt,
            ytelse = ytelse,
            saksnummer = saksnummer,
            vedtakReferanse = vedtakRef,
            ytelseStatus = status,
            kildesystem = kildesystem,
            periode = periode,
            tilleggsopplysninger = tilleggsopplysninger,
            anvist = anvist,
            innhentet = innhentet,
        )

    fun anvist(
        periode: Periode = Periode(fraOgMed = 1.januar(2022), tilOgMed = 31.januar(2022)),
        beløp: BigDecimal = 100.toBigDecimal(),
        dagsats: BigDecimal = 100.toBigDecimal(),
        utbetalingsgrad: BigDecimal = 100.toBigDecimal(),
    ): ForeldrepengerVedtak.ForeldrepengerAnvisning =
        ForeldrepengerVedtak.ForeldrepengerAnvisning(
            periode = periode,
            beløp = beløp,
            dagsats = dagsats,
            utbetalingsgrad = utbetalingsgrad,
        )
}
