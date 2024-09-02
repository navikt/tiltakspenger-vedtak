package no.nav.tiltakspenger.vedtak.repository.vedtak

import io.ktor.server.plugins.NotFoundException
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionContext.Companion.withSession
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class RammevedtakRepoImpl(
    private val behandlingRepo: BehandlingRepo,
    private val sessionFactory: PostgresSessionFactory,
) : RammevedtakRepo,
    VedtakDAO {
    override fun hent(vedtakId: VedtakId): Rammevedtak? =
        sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        sqlHent,
                        mapOf(
                            "id" to vedtakId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(sessionContext)
                    }.asSingle,
                )
            }
        }

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): Rammevedtak =
        sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        sqlHentForBehandling,
                        mapOf(
                            "behandlingId" to behandlingId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(sessionContext)
                    }.asSingle,
                )
            }
        } ?: throw NotFoundException("Ikke funnet")

    override fun hentVedtakForSak(
        sakId: SakId,
        sessionContext: SessionContext,
    ): List<Rammevedtak> =
        sessionContext.withSession { session ->
            session.run(
                queryOf(
                    sqlHentForSak,
                    mapOf(
                        "sakId" to sakId.toString(),
                    ),
                ).map { row ->
                    row.toVedtak(sessionContext)
                }.asList,
            )
        }

    override fun lagreVedtak(
        vedtak: Rammevedtak,
        context: TransactionContext?,
    ): Rammevedtak =
        sessionFactory.withTransaction(context) { tx ->
            lagreVedtak(vedtak, tx)
        }

    override fun lagreVedtak(
        vedtak: Rammevedtak,
        tx: TransactionalSession,
    ): Rammevedtak {
        tx.run(
            queryOf(
                sqlLagre,
                mapOf(
                    "id" to vedtak.id.toString(),
                    "sakId" to vedtak.sakId.toString(),
                    "behandlingId" to vedtak.behandling.id.toString(),
                    "vedtakstype" to vedtak.vedtaksType.toString(),
                    "vedtaksdato" to vedtak.vedtaksdato,
                    "fom" to vedtak.periode.fraOgMed,
                    "tom" to vedtak.periode.tilOgMed,
                    "saksbehandler" to vedtak.saksbehandler,
                    "beslutter" to vedtak.beslutter,
                    "opprettet" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
        return vedtak
    }

    private fun Row.toVedtak(sessionContext: SessionContext): Rammevedtak {
        val id = VedtakId.fromString(string("id"))
        return Rammevedtak(
            id = id,
            sakId = SakId.fromString(string("sak_id")),
            saksnummer = Saksnummer(string("saksnummer")),
            behandling = behandlingRepo.hent(BehandlingId.fromString(string("behandling_id")), sessionContext),
            vedtaksdato = localDateTime("vedtaksdato"),
            vedtaksType = VedtaksType.valueOf(string("vedtakstype")),
            periode = Periode(fraOgMed = localDate("fom"), tilOgMed = localDate("tom")),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
        )
    }

    @Language("SQL")
    private val sqlHent =
        """
        select v.*, s.saksnummer from rammevedtak v join sak s on s.id = v.sak_id where v.id = :id
        """.trimIndent()

    @Language("SQL")
    private val sqlHentForBehandling =
        """
        select v.*, s.saksnummer from rammevedtak v join sak s on s.id = v.sak_id where v.behandling_id = :behandlingId
        """.trimIndent()

    @Language("SQL")
    private val sqlHentForSak =
        """
        select v.*, s.saksnummer from rammevedtak v join sak s on s.id = v.sak_id where v.sak_id = :sakId
        """.trimIndent()

    @Language("SQL")
    private val sqlLagre =
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
        """.trimIndent()
}
