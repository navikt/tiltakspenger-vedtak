package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.AttesteringId
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.attestering.AttesteringStatus
import no.nav.tiltakspenger.vedtak.db.DataSource
import org.intellij.lang.annotations.Language

internal class AttesteringRepoImpl : AttesteringRepo {
    override fun lagre(attestering: no.nav.tiltakspenger.saksbehandling.attestering.Attestering): no.nav.tiltakspenger.saksbehandling.attestering.Attestering {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                lagre(attestering, txSession)
            }
        }
    }

    override fun lagre(attestering: no.nav.tiltakspenger.saksbehandling.attestering.Attestering, tx: TransactionalSession): no.nav.tiltakspenger.saksbehandling.attestering.Attestering {
        tx.run(
            queryOf(
                sqlLagre,
                mapOf(
                    "id" to attestering.id.toString(),
                    "behandlingId" to attestering.behandlingId.toString(),
                    "svar" to attestering.svar.toString(),
                    "begrunnelse" to attestering.begrunnelse,
                    "beslutter" to attestering.beslutter,
                    "tidspunkt" to attestering.tidspunkt,
                ),
            ).asUpdate,
        )
        return attestering
    }

    override fun hentForBehandling(behandlingId: BehandlingId): List<no.nav.tiltakspenger.saksbehandling.attestering.Attestering> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForBehandling,
                        mapOf(
                            "behandlingId" to behandlingId.toString(),
                        ),
                    ).map { row ->
                        row.toAttestering()
                    }.asList,
                )
            }
        }
    }

    @Language("SQL")
    private val sqlHentForBehandling = """
        select * from attestering where behandling_id = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlLagre = """
        insert into attestering (
            id, 
            behandling_id, 
            svar,
            begrunnelse,
            beslutter,
            tidspunkt
        ) values (
            :id, 
            :behandlingId, 
            :svar,
            :begrunnelse,
            :beslutter,
            :tidspunkt
        )
    """.trimIndent()

    private fun Row.toAttestering() = no.nav.tiltakspenger.saksbehandling.attestering.Attestering(
        id = AttesteringId.fromDb(string("id")),
        behandlingId = BehandlingId.fromDb(string("behandling_id")),
        svar = no.nav.tiltakspenger.saksbehandling.attestering.AttesteringStatus.valueOf(string("svar")),
        begrunnelse = stringOrNull("begrunnelse"),
        beslutter = string("beslutter"),
        tidspunkt = localDateTime("tidspunkt"),
    )
}
