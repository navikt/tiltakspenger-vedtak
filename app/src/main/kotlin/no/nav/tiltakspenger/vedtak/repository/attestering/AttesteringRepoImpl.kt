package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.AttesteringId
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import org.intellij.lang.annotations.Language

internal class AttesteringRepoImpl(
    private val sessionFactory: PostgresSessionFactory,
) : AttesteringRepo, AttesteringDAO {

    // Attestering lagres alltid sammen med Behandling.
    override fun lagre(attestering: Attestering, context: TransactionContext?): Attestering {
        return sessionFactory.withTransaction(context) { tx ->
            lagre(attestering, tx)
        }
    }

    override fun lagre(attestering: Attestering, tx: TransactionalSession): Attestering {
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

    override fun hentForBehandling(behandlingId: BehandlingId): List<Attestering> {
        return sessionFactory.withTransaction { tx ->
            tx.run(
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

    private fun Row.toAttestering() = Attestering(
        id = AttesteringId.fromDb(string("id")),
        behandlingId = BehandlingId.fromString(string("behandling_id")),
        svar = AttesteringStatus.valueOf(string("svar")),
        begrunnelse = stringOrNull("begrunnelse"),
        beslutter = string("beslutter"),
        tidspunkt = localDateTime("tidspunkt"),
    )
}
