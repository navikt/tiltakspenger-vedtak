package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class VedtakRepoImpl(
    private val behandlingRepo: BehandlingRepo,
    private val saksopplysningRepo: SaksopplysningRepo,
    private val vurderingRepo: VurderingRepo,
) : VedtakRepo {
    override fun hent(vedtakId: VedtakId): Vedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHent,
                        mapOf(
                            "id" to vedtakId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForBehandling,
                        mapOf(
                            "behandlingId" to behandlingId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asList,
                )
            }
        }
    }

    override fun lagreVedtak(vedtak: Vedtak): Vedtak {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to vedtak.id.toString(),
                            "behandlingId" to vedtak.behandling.id.toString(),
                            "vedtakstype" to vedtak.vedtaksType.toString(),
                            "vedtaksdato" to vedtak.vedtaksdato,
                            "fom" to vedtak.periode.fra,
                            "tom" to vedtak.periode.til,
                            "saksbehandler" to vedtak.saksbehandler,
                            "beslutter" to vedtak.beslutter,
                            "opprettet" to LocalDateTime.now(),
                        ),
                    ).asUpdate,
                )
                saksopplysningRepo.lagre(vedtak.id, vedtak.saksopplysninger, txSession)
                vurderingRepo.lagre(vedtak.id, vedtak.vurderinger, txSession)
            }
        }
        return vedtak
    }

    private fun Row.toVedtak(txSession: TransactionalSession): Vedtak {
        val id = VedtakId.fromDb(string("id"))
        return Vedtak(
            id = id,
            behandling = behandlingRepo.hent(BehandlingId.fromDb(string("behandling_id"))) as BehandlingIverksatt,
            vedtaksdato = localDate("vedtaksdato"),
            vedtaksType = VedtaksType.valueOf(string("vedtakstype")),
            periode = Periode(fra = localDate("fom"), til = localDate("tom")),
            saksopplysninger = saksopplysningRepo.hent(id, txSession),
            vurderinger = vurderingRepo.hent(id, txSession),
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
    private val sqlLagre = """
        insert into vedtak (
            id, 
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
