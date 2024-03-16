package no.nav.tiltakspenger.vedtak.repository.foreldrepenger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

class ForeldrepengerVedtakDAO(
    private val foreldrepengerAnvisningDAO: ForeldrepengerAnvisningDAO = ForeldrepengerAnvisningDAO(),
) {
    fun hentForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession): List<ForeldrepengerVedtak> {
        return txSession.run(
            queryOf(hentForeldrepengerVedtak, innsendingId.toString())
                .map { row -> row.toForeldrepengerVedtak(txSession) }
                .asList,
        )
    }

    fun lagre(
        innsendingId: InnsendingId,
        foreldrepengerVedtak: List<ForeldrepengerVedtak>,
        txSession: TransactionalSession,
    ) {
        slettAlleForeldrepengerVedtak(innsendingId, txSession)
        foreldrepengerVedtak.forEach { vedtak ->
            lagreForeldrepengerVedtak(innsendingId, vedtak, txSession)
        }
    }

    private fun lagreForeldrepengerVedtak(
        innsendingId: InnsendingId,
        foreldrepengerVedtak: ForeldrepengerVedtak,
        txSession: TransactionalSession,
    ) {
        val id = ForeldrepengerVedtakId.random()
        txSession.run(
            queryOf(
                lagreForeldrepengerVedtak,
                mapOf(
                    "id" to id.toString(),
                    "innsendingId" to innsendingId.toString(),
                    "version" to foreldrepengerVedtak.version,
                    "aktor" to foreldrepengerVedtak.aktør,
                    "vedtattTidspunkt" to foreldrepengerVedtak.vedtattTidspunkt,
                    "ytelse" to foreldrepengerVedtak.ytelse.name,
                    "saksnummer" to foreldrepengerVedtak.saksnummer,
                    "vedtakReferanse" to foreldrepengerVedtak.vedtakReferanse,
                    "ytelseStatus" to foreldrepengerVedtak.ytelseStatus.name,
                    "kildesystem" to foreldrepengerVedtak.kildesystem.name,
                    "fra" to foreldrepengerVedtak.periode.fra,
                    "til" to foreldrepengerVedtak.periode.til,
                    "tilleggsopplysninger" to foreldrepengerVedtak.tilleggsopplysninger,
                    "innhentet" to foreldrepengerVedtak.innhentet,
                    "tidsstempelHosOss" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
        foreldrepengerAnvisningDAO.lagre(id, foreldrepengerVedtak.anvist, txSession)
    }

    private fun slettAlleForeldrepengerVedtak(innsendingId: InnsendingId, txSession: TransactionalSession) {
        val foreldrepengerVedtak = hentForInnsending(innsendingId, txSession)
        foreldrepengerVedtak.forEach {
            foreldrepengerAnvisningDAO.slettForeldrepengerAnvisninger(it.id, txSession)
        }
        txSession.run(queryOf(slettForeldrepengerVedtak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toForeldrepengerVedtak(txSession: TransactionalSession): ForeldrepengerVedtak {
        val id = ForeldrepengerVedtakId.fromDb(string("id"))
        return ForeldrepengerVedtak(
            id = id,
            version = string("version"),
            aktør = string("aktør"),
            vedtattTidspunkt = localDateTime("vedtatt_tidspunkt"),
            ytelse = ForeldrepengerVedtak.Ytelser.valueOf(string("ytelse")),
            saksnummer = stringOrNull("saksnummer"),
            vedtakReferanse = string("vedtakReferanse"),
            ytelseStatus = ForeldrepengerVedtak.Status.valueOf(string("ytelseStatus")),
            kildesystem = ForeldrepengerVedtak.Kildesystem.valueOf(string("kildesystem")),
            periode = Periode(fra = localDate("fra"), til = localDate("til")),
            tilleggsopplysninger = stringOrNull("tilleggsopplysninger"),
            anvist = foreldrepengerAnvisningDAO.hentForForeldrepengerVedtak(
                foreldrepengerVedtakId = id,
                txSession = txSession,
            ),
            innhentet = localDateTime("innhentet"),
        )
    }

    @Language("SQL")
    private val lagreForeldrepengerVedtak = """
        insert into foreldrepenger_vedtak (
            id,
            innsending_id,
            version,
            aktør,
            vedtatt_tidspunkt,
            ytelse,
            saksnummer,
            vedtakReferanse,
            ytelseStatus,
            kildesystem,
            fra,
            til,
            tilleggsopplysninger,
            innhentet,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :innsendingId,
            :version,
            :aktor,
            :vedtattTidspunkt,
            :ytelse,
            :saksnummer,
            :vedtakReferanse,
            :ytelseStatus,
            :kildesystem,
            :fra,
            :til,
            :tilleggsopplysninger,
            :innhentet,
            :tidsstempelHosOss
        )
    """.trimIndent()

    @Language("SQL")
    private val slettForeldrepengerVedtak = "delete from foreldrepenger_vedtak where innsending_id = ?"

    @Language("SQL")
    private val hentForeldrepengerVedtak = "select * from foreldrepenger_vedtak where innsending_id = ?"
}
