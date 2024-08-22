package no.nav.tiltakspenger.objectmothers

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortperiode
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsdag
import java.time.LocalDate

interface MeldekortMother {

    fun meldekort(
        id: MeldekortId = MeldekortId.random(),
        sakId: SakId = SakId.random(),
        rammevedtakId: VedtakId = VedtakId.random(),
        meldekortperiode: Meldekortperiode = meldekortperiode(
            meldekortId = id,
        ),
        saksbehandler: String = "saksbehandler",
        beslutter: String = "beslutter",
    ) = UtfyltMeldekort(
        id = id,
        sakId = sakId,
        rammevedtakId = rammevedtakId,
        meldekortperiode = meldekortperiode,
        saksbehandler = saksbehandler,
        beslutter = beslutter,
    )

    /**
     * @param startDato Må starte på en mandag.
     */
    fun meldekortperiode(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        status: Utbetalingsdag.Status = Utbetalingsdag.Status.FullUtbetaling,
        meldekortId: MeldekortId = MeldekortId.random(),
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
    ): Meldekortperiode {
        return Meldekortperiode(
            maksAntallTiltaksdagerIMeldekortperiode(startDato, status, meldekortId, tiltakstype),
        )
    }

    fun maksAntallTiltaksdagerIMeldekortperiode(
        startDato: LocalDate,
        status: Utbetalingsdag.Status,
        meldekortId: MeldekortId,
        tiltakstype: TiltakstypeSomGirRett,
    ): NonEmptyList<Meldekortdag> {
        return (
            tiltaksdager(startDato, status, meldekortId, tiltakstype) +
                ikkeTiltaksdager(startDato.plusDays(5), meldekortId, 2) +
                tiltaksdager(startDato.plusDays(7), status, meldekortId, tiltakstype) +
                ikkeTiltaksdager(startDato.plusDays(12), meldekortId, 2)
            ).toNonEmptyListOrNull()!!
    }

    fun tiltaksdager(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        status: Utbetalingsdag.Status = Utbetalingsdag.Status.FullUtbetaling,
        meldekortId: MeldekortId = MeldekortId.random(),
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        antallDager: Int = 5,
    ): NonEmptyList<Meldekortdag.Tiltaksdag> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.Tiltaksdag(
                dato = startDato.plusDays(index.toLong()),
                status = status,
                meldekortId = meldekortId,
                tiltakstype = tiltakstype,
            )
        }.toNonEmptyListOrNull()!!
    }

    fun ikkeTiltaksdager(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        meldekortId: MeldekortId = MeldekortId.random(),
        antallDager: Int = 2,
    ): NonEmptyList<Meldekortdag.IkkeTiltaksdag> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.IkkeTiltaksdag(
                dato = startDato.plusDays(index.toLong()),
                meldekortId = meldekortId,
            )
        }.toNonEmptyListOrNull()!!
    }
}
