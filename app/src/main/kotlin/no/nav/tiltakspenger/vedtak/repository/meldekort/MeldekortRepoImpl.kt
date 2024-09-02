package no.nav.tiltakspenger.vedtak.repository.meldekort

import arrow.core.toNonEmptyListOrNull
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.BehandlingId
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
import no.nav.tiltakspenger.meldekort.domene.Meldekortperioder
import no.nav.tiltakspenger.meldekort.domene.tilMeldekortperioder
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

class MeldekortRepoImpl(
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
                        type
                    ) values (
                        :id,
                        :sakId,
                        :rammevedtakId,
                        :fraOgMed,
                        :tilOgMed,
                        to_jsonb(:meldekortdager::jsonb),
                        :saksbehandler,
                        :beslutter,
                        :type
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
                        "type" to
                            when (meldekort) {
                                is UtfyltMeldekort -> "utfylt"
                                is IkkeUtfyltMeldekort -> "ikke_utfylt"
                            },
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

    internal fun hentForMeldekortId(
        meldekortId: MeldekortId,
        session: Session,
    ): Meldekort? =
        session.run(
            queryOf(
                """
                select * from meldekort where id = :id
                """.trimIndent(),
                mapOf("id" to meldekortId.toString()),
            ).map { row ->
                fromRow(row)
            }.asSingle,
        )

    override fun hentforSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): Meldekortperioder? =
        sessionFactory
            .withSession(sessionContext) { session ->
                session.run(
                    queryOf(
                        """
                        select * from meldekort where sakId = :sakId
                        """.trimIndent(),
                        mapOf("sakId" to sakId),
                    ).map { row ->
                        fromRow(row)
                    }.asList,
                )
            }.let { it.toNonEmptyListOrNull()?.tilMeldekortperioder() }

    override fun hentSammendragforSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): List<MeldekortSammendrag> =
        sessionFactory
            .withSession(sessionContext) { session ->
                session.run(
                    queryOf(
                        "select id,fom,tom,type from meldekort where sakId = :sakId",
                        mapOf("sakId" to sakId),
                    ).map { row ->
                        MeldekortSammendrag(
                            meldekortId = MeldekortId.fromString(row.string("id")),
                            periode =
                            Periode(
                                fraOgMed = row.localDate("fom"),
                                tilOgMed = row.localDate("tom"),
                            ),
                            erUtfylt = row.string("type") == "utfylt",
                        )
                    }.asList,
                )
            }

    override fun hentFnrForMeldekortId(
        meldekortId: MeldekortId,
        sessionContext: SessionContext?,
    ): Fnr? =
        sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf(
                    """
                    select sak.fnr from sak
                    join meldekort on sak.id = meldekort.sakId
                    where meldekort.id = :meldekortId
                    """.trimIndent(),
                    mapOf("meldekortId" to meldekortId.toString()),
                ).map { row ->
                    Fnr.fromString(row.string("fnr"))
                }.asSingle,
            )
        }

    override fun hentUtfylteMeldekortForFørstegangsbehandlingId(førstegangsbehandlingId: BehandlingId): Meldekortperioder? =
        sessionFactory
            .withSession { session ->
                session.run(
                    queryOf(
                        """
                        select * from meldekort m 
                        join rammvedtak r on m.rammevedtakId = r.id 
                        where r.behandling_id = :rammevedtakId and type = 'utfylt'
                        """.trimIndent(),
                        mapOf("rammevedtakId" to førstegangsbehandlingId.toString()),
                    ).map { row ->
                        fromRow(row)
                    }.asList,
                )
            }.toNonEmptyListOrNull()
            ?.let {
                Meldekortperioder(
                    tiltakstype = it.first().tiltakstype,
                    verdi = it,
                )
            }

    private fun fromRow(row: Row): Meldekort {
        val id = MeldekortId.fromString(row.string("id"))
        val sakId = SakId.fromString(row.string("sakId"))
        return when (val type = row.string("type")) {
            "utfylt" -> {
                val meldekortperiode = row.string("meldekortdager").toUtfyltMeldekortperiode(sakId, id)
                UtfyltMeldekort(
                    id = id,
                    sakId = sakId,
                    rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                    meldekortperiode = meldekortperiode,
                    saksbehandler = row.string("saksbehandler"),
                    beslutter = row.string("beslutter"),
                    forrigeMeldekortId = row.stringOrNull("forrigeMeldekortId")?.let { MeldekortId.fromString(it) },
                    tiltakstype = meldekortperiode.tiltakstype,
                )
            }

            "ikke_utfylt" -> {
                val meldekortperiode = row.string("meldekortdager").toIkkeUtfyltMeldekortperiode(sakId, id)
                IkkeUtfyltMeldekort(
                    id = id,
                    sakId = sakId,
                    rammevedtakId = VedtakId.fromString(row.string("rammevedtakId")),
                    meldekortperiode = meldekortperiode,
                    forrigeMeldekortId = MeldekortId.fromString(row.string("forrigeMeldekortId")),
                    tiltakstype = meldekortperiode.tiltakstype,
                )
            }

            else -> throw IllegalStateException("Ukjent meldekorttype $type for meldekort $id")
        }
    }
}
