package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

internal class KravdatoSaksopplysningRepo {
    fun hentKravdatoFraSaksbehandler(behandlingId: BehandlingId, txSession: TransactionalSession): KravdatoSaksopplysning? {
        return txSession.run(
            queryOf(
                sqlHentKravdatoFraKilde,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "kilde" to Kilde.SAKSB.name,
                ),
            ).map { row ->
                row.toKravdatoSaksopplysning()
            }.asSingle,
        )
    }

    fun hentKravdatoFraSøknad(behandlingId: BehandlingId, txSession: TransactionalSession): KravdatoSaksopplysning? {
        return txSession.run(
            queryOf(
                sqlHentKravdatoFraKilde,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "kilde" to Kilde.SØKNAD.name,
                ),
            ).map { row ->
                row.toKravdatoSaksopplysning()
            }.asSingle,
        )
    }

    fun hentAvklartKravdato(behandlingId: BehandlingId, txSession: TransactionalSession): KravdatoSaksopplysning? {
        return txSession.run(
            queryOf(
                sqlHentAvklartKravdato,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                ),
            ).map { row ->
                row.toKravdatoSaksopplysning()
            }.asSingle,
        )
    }

    private fun Row.toKravdatoSaksopplysning(): KravdatoSaksopplysning =
        KravdatoSaksopplysning(
            kravdato = localDate("kravdato"),
            kilde = Kilde.valueOf(string("datakilde")),
            saksbehandlerIdent = stringOrNull("saksbehandler"),
        )

    private val sqlHentKravdatoFraKilde = """
        select * from kravdato_saksopplysning where behandling_id = :behandlingId and datakilde = :kilde
    """.trimIndent()

    private val sqlHentAvklartKravdato = """
        select * from kravdato_saksopplysning where behandling_id = :behandlingId and avklart_tidspunkt is not null
    """.trimIndent()
}
