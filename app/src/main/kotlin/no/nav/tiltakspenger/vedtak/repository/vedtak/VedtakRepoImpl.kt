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
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.UtfallsperiodeDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import no.nav.tiltakspenger.vedtak.service.ports.BehandlingRepo
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class VedtakRepoImpl(
    private val behandlingRepo: BehandlingRepo = PostgresBehandlingRepo(),
    private val saksopplysningRepo: SaksopplysningRepo = SaksopplysningRepo(),
    private val vurderingRepo: VurderingRepo = VurderingRepo(),
    private val utfallsperiodeDAO: UtfallsperiodeDAO = UtfallsperiodeDAO(),
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

    override fun hentVedtakForSak(sakId: SakId): List<Vedtak> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForSak,
                        mapOf(
                            "sakId" to sakId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asList,
                )
            }
        }
    }

    override fun lagreVedtak(vedtak: Vedtak): Vedtak {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                lagreVedtak(vedtak, txSession)
            }
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
                    "fom" to vedtak.periode.fra,
                    "tom" to vedtak.periode.til,
                    "saksbehandler" to vedtak.saksbehandler,
                    "beslutter" to vedtak.beslutter,
                    "opprettet" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
        saksopplysningRepo.lagre(vedtak.id, vedtak.saksopplysninger, tx)
        vurderingRepo.lagre(vedtak.id, vedtak.vurderinger, tx)
        utfallsperiodeDAO.oppdaterVedtak(vedtak.id, vedtak.behandling.id, tx)
        return vedtak
    }

    private fun Row.toVedtak(txSession: TransactionalSession): Vedtak {
        val id = VedtakId.fromDb(string("id"))
        return Vedtak(
            id = id,
            sakId = SakId.fromDb(string("sak_id")),
            behandling = behandlingRepo.hent(BehandlingId.fromDb(string("behandling_id"))) as BehandlingIverksatt,
            vedtaksdato = localDateTime("vedtaksdato"),
            vedtaksType = VedtaksType.valueOf(string("vedtakstype")),
            periode = Periode(fra = localDate("fom"), til = localDate("tom")),
            saksopplysninger = saksopplysningRepo.hent(id, txSession),
            utfallsperioder = utfallsperiodeDAO.hentForVedtak(id, txSession),
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
