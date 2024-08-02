package no.nav.tiltakspenger.vedtak.repository.vedtak

import io.ktor.server.plugins.NotFoundException
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class VedtakRepoImpl(
    private val behandlingRepo: BehandlingRepo,
    private val sessionFactory: PostgresSessionFactory,
) : VedtakRepo, VedtakDAO {
    override fun hent(vedtakId: VedtakId): Vedtak? {
        return sessionFactory.withTransaction { tx ->
            tx.run(
                queryOf(
                    sqlHent,
                    mapOf(
                        "id" to vedtakId.toString(),
                    ),
                ).map { row ->
                    row.toVedtak(tx)
                }.asSingle,
            )
        }
    }

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak {
        return sessionFactory.withTransaction { tx ->
            tx.run(
                queryOf(
                    sqlHentForBehandling,
                    mapOf(
                        "behandlingId" to behandlingId.toString(),
                    ),
                ).map { row ->
                    row.toVedtak(tx)
                }.asSingle,
            )
        } ?: throw NotFoundException("Ikke funnet")
    }

    override fun hentVedtakForSak(sakId: SakId, tx: TransactionalSession): List<Vedtak> {
        return tx.run(
            queryOf(
                sqlHentForSak,
                mapOf(
                    "sakId" to sakId.toString(),
                ),
            ).map { row ->
                row.toVedtak(tx)
            }.asList,
        )
    }

    override fun lagreVedtak(vedtak: Vedtak, context: TransactionContext?): Vedtak {
        return sessionFactory.withTransaction(context) { tx ->
            lagreVedtak(vedtak, tx)
        }
    }

    override fun lagreVedtak(vedtak: Vedtak, tx: TransactionalSession): Vedtak {
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

    private fun Row.toVedtak(txSession: TransactionalSession): Vedtak {
        val id = VedtakId.fromDb(string("id"))
        return Vedtak(
            id = id,
            sakId = SakId.fromString(string("sak_id")),
            behandling = behandlingRepo.hent(BehandlingId.fromString(string("behandling_id"))),
            vedtaksdato = localDateTime("vedtaksdato"),
            vedtaksType = VedtaksType.valueOf(string("vedtakstype")),
            periode = Periode(fraOgMed = localDate("fom"), tilOgMed = localDate("tom")),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
        )
    }

    @Language("SQL")
    private val sqlHent = """
        select * from vedtak where id = :id
    """.trimIndent()

    @Language("SQL")
    private val sqlHentForBehandling = """
        select * from vedtak where behandling_id = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentForSak = """
        select * from vedtak where sak_id = :sakId
    """.trimIndent()

    @Language("SQL")
    private val sqlLagre = """
        insert into vedtak (
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
