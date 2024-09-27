package no.nav.tiltakspenger.vedtak.repository.meldekort

import arrow.core.toNonEmptyListOrNull
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort.IkkeUtfyltMeldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortSammendrag
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.meldekort.domene.tilMeldekortperioder
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

class MeldekortPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : MeldekortRepo {
    override fun lagre(
        meldekort: Meldekort,
        transactionContext: TransactionContext?,
    ) {
        sessionFactory.withTransaction(transactionContext) { tx ->
            tx.run(
                queryOf(
                    """
                    insert into meldekort (
                        id,
                        sakId,
                        rammevedtakId,
                        fraOgMed,
                        tilOgMed,
                        meldekortdager,
                        saksbehandler,
                        beslutter,
                        status
                    ) values (
                        :id,
                        :sakId,
                        :rammevedtakId,
                        :fraOgMed,
                        :tilOgMed,
                        to_jsonb(:meldekortdager::jsonb),
                        :saksbehandler,
                        :beslutter,
                        :status
                    )
                    """.trimIndent(),
                    mapOf(
                        "id" to meldekort.id.toString(),
                        "sakId" to meldekort.sakId.toString(),
                        "rammevedtakId" to meldekort.rammevedtakId.toString(),
                        "fraOgMed" to meldekort.fraOgMed,
                        "tilOgMed" to meldekort.periode.tilOgMed,
                        "meldekortdager" to meldekort.meldekortperiode.toDbJson(),
                        "saksbehandler" to meldekort.saksbehandler,
                        "beslutter" to meldekort.beslutter,
                        "status" to meldekort.status.toDb(),
                    ),
                ).asUpdate,
            )
        }
    }

    override fun oppdater(
        meldekort: Meldekort,
        transactionContext: TransactionContext?,
    ) {
        sessionFactory.withTransaction(transactionContext) { tx ->
            tx.run(
                queryOf(
                    """
                    update meldekort set 
                        meldekortdager = to_jsonb(:meldekortdager::jsonb),
                        saksbehandler = :saksbehandler,
                        beslutter = :beslutter,
                        status = :status
                    where id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to meldekort.id.toString(),
                        "meldekortdager" to meldekort.meldekortperiode.toDbJson(),
                        "saksbehandler" to meldekort.saksbehandler,
                        "beslutter" to meldekort.beslutter,
                        "status" to meldekort.status.toDb(),
                    ),
                ).asUpdate,
            )
        }
    }

    override fun hentForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): Meldekort? =
        sessionFactory.withSession(sessionContext) { session ->
            hentForMeldekortId(meldekortId, session)
        }

    override fun hentForSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): Meldeperioder? {
        return sessionFactory.withSession(sessionContext) { session ->
            hentForSakId(sakId, session)
        }
    }

    override fun hentSammendragforSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): List<MeldekortSammendrag> {
        return sessionFactory
            .withSession(sessionContext) { session ->
                session.run(
                    queryOf(
                        "select id,fraOgMed,tilOgMed,status,beslutter,saksbehandler from meldekort where sakId = :sakId",
                        mapOf("sakId" to sakId.toString()),
                    ).map { row ->
                        MeldekortSammendrag(
                            meldekortId = MeldekortId.fromString(row.string("id")),
                            periode =
                            Periode(
                                fraOgMed = row.localDate("fraOgMed"),
                                tilOgMed = row.localDate("tilOgMed"),
                            ),
                            status = row.string("status").toMeldekortStatus(),
                            beslutter = row.stringOrNull("beslutter"),
                            saksbehandler = row.stringOrNull("saksbehandler"),
                        )
                    }.asList,
                )
            }
    }

    override fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): Fnr? =
        sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf(
                    """
                    select sak.ident as fnr from sak
                    join meldekort on sak.id = meldekort.sakId
                    where meldekort.id = :meldekortId
                    """.trimIndent(),
                    mapOf("meldekortId" to meldekortId.toString()),
                ).map { row ->
                    Fnr.fromString(row.string("fnr"))
                }.asSingle,
            )
        }

    companion object {
        internal fun hentForMeldekortId(
            meldekortId: MeldekortId,
            session: Session,
        ): Meldekort? {
            return session.run(
                queryOf(
                    """
                        select m.*,s.ident as fnr from meldekort m join sak s on s.id = m.sakId where m.id = :id
                    """.trimIndent(),
                    mapOf("id" to meldekortId.toString()),
                ).map { row ->
                    fromRow(row)
                }.asSingle,
            )
        }
        internal fun hentForSakId(
            sakId: SakId,
            session: Session,
        ): Meldeperioder? {
            return session.run(
                queryOf(
                    "select m.*,s.ident as fnr from meldekort m join sak s on s.id = m.sakId where s.id = :sakId",
                    mapOf("sakId" to sakId.toString()),
                ).map { fromRow(it) }.asList,
            ).let { it.toNonEmptyListOrNull()?.tilMeldekortperioder() }
        }

        private fun fromRow(row: Row): Meldekort {
            val id = MeldekortId.fromString(row.string("id"))
            val sakId = SakId.fromString(row.string("sakId"))
            return when (val status = row.string("status")) {
                "GODKJENT", "KLAR_TIL_BESLUTNING" -> {
                    val meldekortperiode = row.string("meldekortdager").toUtfyltMeldekortperiode(sakId, id)
                    UtfyltMeldekort(
                        id = id,
                        sakId = sakId,
                        fnr = Fnr.fromString(row.string("fnr")),
                        rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                        meldekortperiode = meldekortperiode,
                        saksbehandler = row.string("saksbehandler"),
                        beslutter = row.stringOrNull("beslutter"),
                        forrigeMeldekortId = row.stringOrNull("forrigeMeldekortId")?.let { MeldekortId.fromString(it) },
                        tiltakstype = meldekortperiode.tiltakstype,
                        status = row.string("status").toMeldekortStatus(),
                        iverksattTidspunkt = row.localDateTimeOrNull("iverksatt_tidspunkt"),
                    )
                }
                "KLAR_TIL_UTFYLLING" -> {
                    val meldekortperiode = row.string("meldekortdager").toIkkeUtfyltMeldekortperiode(sakId, id)
                    IkkeUtfyltMeldekort(
                        id = id,
                        sakId = sakId,
                        fnr = Fnr.fromString(row.string("fnr")),
                        rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                        meldekortperiode = meldekortperiode,
                        forrigeMeldekortId = row.stringOrNull("forrigeMeldekortId")?.let { MeldekortId.fromString(it) },
                        tiltakstype = meldekortperiode.tiltakstype,
                    )
                }

                else -> throw IllegalStateException("Ukjent meldekortstatus $status for meldekort $id")
            }
        }
    }
}
