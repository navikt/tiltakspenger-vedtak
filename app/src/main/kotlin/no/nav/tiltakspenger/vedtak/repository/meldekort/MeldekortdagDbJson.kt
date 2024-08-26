package no.nav.tiltakspenger.vedtak.repository.meldekort

import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortperiode
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsdag
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.tiltak.toDb
import no.nav.tiltakspenger.vedtak.repository.tiltak.toTiltakstypeSomGirRett
import java.time.LocalDate

private data class MeldekortdagDbJson(
    val tiltakstype: String?,
    val dato: String,
    val status: Status,
) {
    enum class Status {
        FullUtbetaling,
        DelvisUtbetaling,
        IngenUtbetaling,
        ;

        fun toDomain(): Utbetalingsdag.Status {
            return when (this) {
                FullUtbetaling -> Utbetalingsdag.Status.FullUtbetaling
                DelvisUtbetaling -> Utbetalingsdag.Status.DelvisUtbetaling
                IngenUtbetaling -> throw IllegalStateException("Ingen utbetaling er ikke en gyldig status for en utbetalingsdag")
            }
        }
    }

    fun toMeldekortdag(meldekortId: MeldekortId): Meldekortdag {
        val parsedDato = LocalDate.parse(dato)
        return if (status == Status.IngenUtbetaling) {
            Meldekortdag.IkkeTiltaksdag(parsedDato, meldekortId)
        } else {
            Meldekortdag.Tiltaksdag(
                dato = parsedDato,
                status = status.toDomain(),
                meldekortId = meldekortId,
                tiltakstype = tiltakstype!!.toTiltakstypeSomGirRett(),
            )
        }
    }
}

internal fun Meldekortperiode.toDbJson(): String {
    return verdi.map {
        MeldekortdagDbJson(
            tiltakstype = tiltakstype.toDb(),
            dato = it.dato.toString(),
            status = when (it) {
                is Meldekortdag.IkkeTiltaksdag -> MeldekortdagDbJson.Status.IngenUtbetaling
                is Meldekortdag.Tiltaksdag -> when (it.status) {
                    Utbetalingsdag.Status.FullUtbetaling -> MeldekortdagDbJson.Status.FullUtbetaling
                    Utbetalingsdag.Status.DelvisUtbetaling -> MeldekortdagDbJson.Status.DelvisUtbetaling
                }
            },
        )
    }.let { serialize(it) }
}

internal fun String.toMeldekortperiode(meldekortId: MeldekortId): Meldekortperiode {
    return deserializeList<MeldekortdagDbJson>(this).map {
        it.toMeldekortdag(meldekortId)
    }.let {
        Meldekortperiode(it.toNonEmptyListOrNull()!!)
    }
}
