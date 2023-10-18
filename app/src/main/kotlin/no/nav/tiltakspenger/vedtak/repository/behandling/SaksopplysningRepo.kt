package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakspplysningId
import no.nav.tiltakspenger.felles.nå
import org.intellij.lang.annotations.Language

internal class SaksopplysningRepo {
    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Saksopplysning> {
        return txSession.run(
            queryOf(
                sqlHentSaksopplysninger,
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
            lagre(behandlingId, saksopplysning, txSession)
        }
    }

    private fun lagre(behandlingId: BehandlingId, saksopplysning: Saksopplysning, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                sqlLagreSaksopplysning,
                mapOf(
                    "id" to SakspplysningId.random().toString(),
                    "behandlingId" to behandlingId.toString(),
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
                sqlSlettSaksopplysninger,
                mapOf("behandlingId" to behandlingId.toString()),
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

    private val sqlHentSaksopplysninger = """
        select * from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    private val sqlSlettSaksopplysninger = """
        delete from saksopplysning where behandlingId = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val sqlLagreSaksopplysning = """
        insert into saksopplysning (
                id,
                behandlingId,
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
