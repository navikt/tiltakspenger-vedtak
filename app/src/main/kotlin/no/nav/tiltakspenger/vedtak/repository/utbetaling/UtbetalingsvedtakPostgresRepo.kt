package no.nav.tiltakspenger.vedtak.repository.utbetaling

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.domene.Meldeperiode
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.toMeldekortStatus
import java.time.LocalDateTime

internal class UtbetalingsvedtakPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : UtbetalingsvedtakRepo {
    override fun lagre(vedtak: Utbetalingsvedtak, context: TransactionContext?) {
        sessionFactory.withSession(context) { session ->
            session.run(
                queryOf(
                    """
                    insert into utbetalingsvedtak (
                        id,
                        sakId,
                        rammevedtakId,
                        brukerNavkontor,
                        vedtakstidspunkt,
                        saksbehandler,
                        beslutter,
                        forrigeVedtakId,
                        meldekortId,
                        utbetalingsperiode
                    ) values (
                        :id,
                        :sakId,
                        :rammevedtakId,
                        :brukerNavnkontor,
                        :vedtakstidspunkt,
                        :saksbehandler,
                        :beslutter,
                        :forrigeVedtakId,
                        :meldekortId,
                        to_jsonb(:utbetalingsperiode::jsonb)
                    )
                    """.trimIndent(),
                    mapOf(
                        "id" to vedtak.id.toString(),
                        "sakId" to vedtak.sakId.toString(),
                        "rammevedtakId" to vedtak.rammevedtakId.toString(),
                        "brukerNavnkontor" to vedtak.brukerNavkontor,
                        "vedtakstidspunkt" to vedtak.vedtakstidspunkt,
                        "saksbehandler" to vedtak.saksbehandler,
                        "beslutter" to vedtak.beslutter,
                        "forrigeVedtakId" to vedtak.forrigeUtbetalingsvedtak?.toString(),
                        "meldekortId" to vedtak.meldekortId.toString(),
                        "utbetalingsperiode" to vedtak.utbetalingsperiode.toDbJson(),
                    ),
                ).asUpdate,
            )
        }
    }

    override fun markerSendtTilUtbetaling(
        vedtakId: VedtakId,
        tidspunkt: LocalDateTime,
        utbetalingsrespons: SendtUtbetaling,
    ) {
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                      update utbetalingsvedtak
                      set sendt_til_utbetaling_tidspunkt = :tidspunkt, 
                          utbetaling_metadata = to_jsonb(:metadata::jsonb)
                      where id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to vedtakId.toString(),
                        "tidspunkt" to tidspunkt,
                        "metadata" to """{"request": "${utbetalingsrespons.request}", "response": "${utbetalingsrespons.response}"}""",
                    ),
                ).asUpdate,
            )
        }
    }

    override fun markerJournalført(
        vedtakId: VedtakId,
        journalpostId: JournalpostId,
        tidspunkt: LocalDateTime,
    ) {
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                      update utbetalingsvedtak 
                      set journalpost_id = :journalpostId,
                          journalføringstidspunkt = :tidspunkt
                      where id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to vedtakId.toString(),
                        "journalpostId" to journalpostId.toString(),
                        "tidspunkt" to tidspunkt,
                    ),
                ).asUpdate,
            )
        }
    }

    override fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak? =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    "select u.*,s.ident as fnr,s.saksnummer from utbetalingsvedtak u join sak s on s.id = u.sakid where u.id = :id",
                    mapOf("id" to vedtakId.toString()),
                ).map { row ->
                    row.toVedtak(session)
                }.asSingle,
            )
        }

    override fun hentForSakId(sakId: SakId): List<Utbetalingsvedtak> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    "select u.*, s.saksnummer, s.ident as fnr from utbetalingsvedtak u join sak s on s.id = u.sakid where u.sakId = :sakId",
                    mapOf("sakId" to sakId.toString()),
                ).map { row ->
                    row.toVedtak(session)
                }.asList,
            )
        }

    override fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): List<Utbetalingsvedtak> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select u.*, s.ident as fnr, s.saksnummer
                    from utbetalingsvedtak u
                    join sak s on s.id = u.sakid
                    join rammevedtak v on u.rammevedtakId = v.id
                    where v.behandling_id = :behandlingId
                    """.trimIndent(),
                    mapOf("behandlingId" to behandlingId.toString()),
                ).map { row ->
                    row.toVedtak(session)
                }.asList,
            )
        }

    override fun hentGodkjenteMeldekortUtenUtbetalingsvedtak(limit: Int): List<UtfyltMeldekort> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select m.*, s.ident as fnr from meldekort m
                    join sak s on s.id = m.sakId
                    left join utbetalingsvedtak u on m.id = u.meldekortId
                    where u.id is null and m.beslutter is not null
                    limit :limit
                    """.trimIndent(),
                    mapOf("limit" to limit),
                ).map { row ->
                    val meldekortperiode =
                        MeldekortPostgresRepo
                            .hentForMeldekortId(
                                MeldekortId.fromString(row.string("id")),
                                session,
                            )!!
                            .meldekortperiode as Meldeperiode.UtfyltMeldeperiode
                    UtfyltMeldekort(
                        id = MeldekortId.fromString(row.string("id")),
                        sakId = SakId.fromString(row.string("sakId")),
                        fnr = Fnr.fromString(row.string("fnr")),
                        rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                        meldekortperiode =
                        meldekortperiode,
                        saksbehandler = row.string("saksbehandler"),
                        beslutter = row.string("beslutter"),
                        forrigeMeldekortId = row.stringOrNull("forrigeMeldekortId")?.let { MeldekortId.fromString(it) },
                        tiltakstype = meldekortperiode.tiltakstype,
                        status = row.string("status").toMeldekortStatus(),
                        iverksattTidspunkt = row.localDateTimeOrNull("iverksatt_tidspunkt"),
                    )
                }.asList,
            )
        }

    override fun hentUtbetalingsvedtakForUtsjekk(limit: Int): List<Utbetalingsvedtak> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select u.*,s.ident as fnr,s.saksnummer 
                    from utbetalingsvedtak u 
                    join sak s on s.id = u.sakid 
                    where u.sendt_til_utbetaling_tidspunkt is null
                    limit :limit
                    """.trimIndent(),
                    mapOf("limit" to limit),
                ).map { row ->
                    row.toVedtak(session)
                }.asList,
            )
        }

    override fun hentDeSomSkalJournalføres(limit: Int): List<Utbetalingsvedtak> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select u.*,s.ident as fnr,s.saksnummer 
                    from utbetalingsvedtak u 
                    join sak s on s.id = u.sakid 
                    where u.journalpost_id is null
                    limit :limit
                    """.trimIndent(),
                    mapOf("limit" to limit),
                ).map { row ->
                    row.toVedtak(session)
                }.asList,
            )
        }

    private fun Row.toVedtak(session: Session): Utbetalingsvedtak {
        val vedtakId = VedtakId.fromString(string("id"))
        return Utbetalingsvedtak(
            id = vedtakId,
            sakId = SakId.fromString(string("sakId")),
            saksnummer = Saksnummer(string("saksnummer")),
            fnr = Fnr.fromString(string("fnr")),
            rammevedtakId = VedtakId.fromString(string("rammevedtakId")),
            brukerNavkontor = string("brukerNavkontor"),
            vedtakstidspunkt = localDateTime("vedtakstidspunkt"),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
            utbetalingsperiode = string("utbetalingsperiode").toUtbetalingsperiode(),
            forrigeUtbetalingsvedtak = stringOrNull("forrigeVedtakId")?.let { VedtakId.fromString(it) },
            meldekort =
            MeldekortPostgresRepo
                .hentForMeldekortId(
                    MeldekortId.fromString(string("meldekortId")),
                    session,
                )!! as UtfyltMeldekort,
            sendtTilUtbetaling = localDateTimeOrNull("sendt_til_utbetaling_tidspunkt"),
            journalpostId = stringOrNull("journalpost_id")?.let { JournalpostId(it) },
            journalføringstidspunkt = localDateTimeOrNull("journalføringstidspunkt"),
        )
    }
}
