package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakspplysningId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import org.intellij.lang.annotations.Language

internal class SaksopplysningRepo {
    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Saksopplysning> {
        return txSession.run(
            queryOf(
                sqlHentForBehandling,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                ),
            ).map { row ->
                row.toSaksopplysning()
            }.asList,
        )
    }

    fun lagre(behandlingId: BehandlingId, saksopplysninger: List<Saksopplysning>, txSession: TransactionalSession) {
        slett(behandlingId, txSession)
        saksopplysninger.forEach { saksopplysning ->
            lagre(
                behandlingId = behandlingId,
                saksopplysning = saksopplysning,
                txSession = txSession,
            )
        }
    }

    private fun lagre(behandlingId: BehandlingId, saksopplysning: Saksopplysning, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlLagreSaksopplysning,
                mapOf(
                    "id" to SakspplysningId.random().toString(),
                    "behandlingId" to behandlingId?.toString(),
                    "vedtakId" to null, // TODO: Fjerne når databasen uansett skal nukes
                    "fom" to saksopplysning.fom,
                    "tom" to saksopplysning.tom,
                    "kilde" to saksopplysning.kilde.name,
                    "vilkar" to saksopplysning.vilkår.tittel, // her burde vi kanskje lage en when over vilkår i stedet for å bruke tittel?
                    "detaljer" to saksopplysning.detaljer,
                    "typeSaksopplysning" to saksopplysning.typeSaksopplysning.name,
                    "saksbehandler" to saksopplysning.saksbehandler,
                    "opprettet" to nå(),
                ),
            ).asUpdate,
        )
    }

    private fun slett(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlSlettForBehandling,
                mapOf("behandlingId" to behandlingId.toString()),
            ).asUpdate,
        )
    }

    private fun slett(vedtakId: VedtakId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlSlettForVedtak,
                mapOf("behandlingId" to vedtakId.toString()),
            ).asUpdate,
        )
    }

    private fun Row.toSaksopplysning(): Saksopplysning {
        val vilkår = hentVilkår(string("vilkår"))
        return Saksopplysning(
            fom = localDate("fom"),
            tom = localDate("tom"),
            kilde = Kilde.valueOf(string("kilde")),
            vilkår = vilkår,
            detaljer = string("detaljer"),
            typeSaksopplysning = TypeSaksopplysning.valueOf(string("typeSaksopplysning")),
            saksbehandler = stringOrNull("saksbehandler"),
        )
    }

    private val sqlHentForBehandling = """
        select * from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    private val sqlHentForVedtak = """
        select * from saksopplysning where vedtakId = :vedtakId
    """.trimIndent()

    private val sqlSlettForBehandling = """
        delete from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    private val sqlSlettForVedtak = """
        delete from saksopplysning where vedtakId = :vedtakId
    """.trimIndent()

    @Language("SQL")
    private val sqlLagreSaksopplysning = """
        insert into saksopplysning (
                id,
                behandlingId,
                vedtakId,
                fom,
                tom,
                kilde,
                vilkår,
                detaljer,
                typeSaksopplysning,
                saksbehandler,
                opprettet
            ) values (
                :id,
                :behandlingId,
                :vedtakId,
                :fom,
                :tom,
                :kilde,
                :vilkar,
                :detaljer,
                :typeSaksopplysning,
                :saksbehandler,
                :opprettet
            )
    """.trimIndent()
}
