package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.distribusjon.domene.VedtakSomSkalDistribueres
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtakstype
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import java.time.LocalDateTime

class RammevedtakPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : RammevedtakRepo {

    override fun hentForVedtakId(vedtakId: VedtakId): Rammevedtak? {
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    "select v.*, s.saksnummer from rammevedtak v join sak s on s.id = v.sak_id where v.id = :id",
                    mapOf(
                        "id" to vedtakId.toString(),
                    ),
                ).map { row ->
                    row.toVedtak(session)
                }.asSingle,
            )
        }
    }

    override fun hentForFnr(fnr: Fnr): List<Rammevedtak> {
        return sessionFactory
            .withSession { session ->
                session.run(
                    queryOf(
                        """
                            select v.*,
                                   s.saksnummer
                              from rammevedtak v
                            join sak s
                              on s.id = v.sak_id 
                            where s.ident = :ident
                        """.trimIndent(),
                        mapOf(
                            "ident" to fnr.verdi,
                        ),
                    ).map { row ->
                        row.toVedtak(session)
                    }.asList,
                )
            }
    }

    override fun hentRammevedtakSomSkalJournalføres(
        limit: Int,
    ): List<Rammevedtak> {
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select v.*, s.saksnummer
                    from rammevedtak v
                    join sak s 
                      on s.id = v.sak_id
                    where v.journalpost_id is null
                    limit :limit
                    """.trimIndent(),
                    mapOf(
                        "limit" to limit,
                    ),
                ).map { row ->
                    row.toVedtak(session)
                }.asList,
            )
        }
    }

    override fun hentRammevedtakSomSkalDistribueres(limit: Int): List<VedtakSomSkalDistribueres> {
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select v.id,v.journalpost_id
                    from rammevedtak v
                    where v.journalpost_id is not null
                    and v.journalføringstidspunkt is not null
                    and v.distribusjonstidspunkt is null
                    and v.distribusjon_id is null
                    limit :limit
                    """.trimIndent(),
                    mapOf(
                        "limit" to limit,
                    ),
                ).map { row ->
                    VedtakSomSkalDistribueres(
                        id = VedtakId.fromString(row.string("id")),
                        journalpostId = JournalpostId(row.string("journalpost_id")),
                    )
                }.asList,
            )
        }
    }

    override fun markerJournalført(id: VedtakId, journalpostId: JournalpostId, tidspunkt: LocalDateTime) {
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    update rammevedtak
                    set journalpost_id = :journalpostId,
                        journalføringstidspunkt = :tidspunkt
                    where id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to id.toString(),
                        "journalpostId" to journalpostId.toString(),
                        "tidspunkt" to tidspunkt,
                    ),
                ).asUpdate,
            )
        }
    }

    override fun markerDistribuert(id: VedtakId, distribusjonId: DistribusjonId, tidspunkt: LocalDateTime) {
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    update rammevedtak
                    set distribusjon_id = :distribusjonId,
                        distribusjonstidspunkt = :tidspunkt
                    where id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to id.toString(),
                        "distribusjonId" to distribusjonId.toString(),
                        "tidspunkt" to tidspunkt,
                    ),
                ).asUpdate,
            )
        }
    }

    override fun lagre(
        vedtak: Rammevedtak,
        context: TransactionContext?,
    ) {
        sessionFactory.withTransaction(context) { tx ->
            lagreVedtak(vedtak, tx)
        }
    }

    companion object {
        fun hentForSakId(
            sakId: SakId,
            session: Session,
        ): Rammevedtak? {
            return session.run(
                queryOf(
                    "select v.*, s.saksnummer from rammevedtak v join sak s on s.id = v.sak_id where v.sak_id = :sakId",
                    mapOf(
                        "sakId" to sakId.toString(),
                    ),
                ).map { row ->
                    row.toVedtak(session)
                }.asSingle,
            )
        }

        internal fun lagreVedtak(
            vedtak: Rammevedtak,
            session: Session,
        ) {
            session.run(
                queryOf(
                    """
                    insert into rammevedtak (
                        id, 
                        sak_id, 
                        behandling_id, 
                        vedtakstype, 
                        vedtaksdato, 
                        fom, 
                        tom, 
                        saksbehandler, 
                        beslutter,
                        opprettet
                    ) values (
                        :id, 
                        :sakId, 
                        :behandlingId, 
                        :vedtakstype, 
                        :vedtaksdato, 
                        :fom, 
                        :tom, 
                        :saksbehandler, 
                        :beslutter,
                        :opprettet
                    )
                    """.trimIndent(),
                    mapOf(
                        "id" to vedtak.id.toString(),
                        "sakId" to vedtak.sakId.toString(),
                        "behandlingId" to vedtak.behandling.id.toString(),
                        "vedtakstype" to vedtak.vedtaksType.toString(),
                        "vedtaksdato" to vedtak.vedtaksdato,
                        "fom" to vedtak.periode.fraOgMed,
                        "tom" to vedtak.periode.tilOgMed,
                        "saksbehandler" to vedtak.saksbehandlerNavIdent,
                        "beslutter" to vedtak.beslutterNavIdent,
                        "opprettet" to LocalDateTime.now(),
                    ),
                ).asUpdate,
            )
        }

        private fun Row.toVedtak(session: Session): Rammevedtak {
            val id = VedtakId.fromString(string("id"))
            return Rammevedtak(
                id = id,
                sakId = SakId.fromString(string("sak_id")),
                saksnummer = Saksnummer(string("saksnummer")),
                behandling =
                BehandlingPostgresRepo.hentOrNull(
                    BehandlingId.fromString(string("behandling_id")),
                    session,
                )!!,
                vedtaksdato = localDateTime("vedtaksdato"),
                vedtaksType = Vedtakstype.valueOf(string("vedtakstype")),
                periode = Periode(fraOgMed = localDate("fom"), tilOgMed = localDate("tom")),
                saksbehandlerNavIdent = string("saksbehandler"),
                beslutterNavIdent = string("beslutter"),
                journalpostId = stringOrNull("journalpost_id")?.let { JournalpostId(it) },
                journalføringstidstpunkt = localDateTimeOrNull("journalføringstidspunkt"),
            )
        }
    }
}
