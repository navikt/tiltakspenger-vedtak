package no.nav.tiltakspenger.vedtak.repository.meldekort

import kotliquery.Session
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

class MeldekortRepoImpl(
    private val sessionFactory: PostgresSessionFactory,
) : MeldekortRepo {
    override fun lagre(meldekort: UtfyltMeldekort) {
        sessionFactory.withTransaction { tx ->
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
                        beslutter
                    ) values (
                        :id,
                        :sakId,
                        :rammevedtakId,
                        :fraOgMed,
                        :tilOgMed,
                        to_jsonb(:meldekortdager::jsonb),
                        :saksbehandler,
                        :beslutter
                    )
                    """.trimIndent(),
                    mapOf(
                        "id" to meldekort.id.toString(),
                        "sakId" to meldekort.sakId.toString(),
                        "rammevedtakId" to meldekort.rammevedtakId.toString(),
                        "fraOgMed" to meldekort.periode.fraOgMed,
                        "tilOgMed" to meldekort.periode.tilOgMed,
                        "meldekortdager" to meldekort.meldekortperiode.toDbJson(),
                        "saksbehandler" to meldekort.saksbehandler,
                        "beslutter" to meldekort.beslutter,
                    ),
                ).asUpdate,
            )
        }
    }

    override fun hentForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): UtfyltMeldekort? {
        return sessionFactory.withSession(sessionContext) { session ->
            hentForMeldekortId(meldekortId, session)
        }
    }

    internal fun hentForMeldekortId(
        meldekortId: MeldekortId,
        session: Session,
    ): UtfyltMeldekort? {
        return session.run(
            queryOf(
                """
                        select * from meldekort where id = :id
                """.trimIndent(),
                mapOf("id" to meldekortId.toString()),
            ).map { row ->
                UtfyltMeldekort(
                    id = MeldekortId.fromString(row.string("id")),
                    sakId = SakId.fromString(row.string("sakId")),
                    rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                    meldekortperiode = row.string("meldekortdager").toMeldekortperiode(meldekortId),
                    saksbehandler = row.string("saksbehandler"),
                    beslutter = row.string("beslutter"),
                )
            }.asSingle,
        )
    }
}
