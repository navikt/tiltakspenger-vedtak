package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

internal class KravdatoSaksopplysningRepo {
    fun lagre(behandlingId: BehandlingId, kravdatoSaksopplysninger: KravdatoSaksopplysninger, txSession: TransactionalSession) {
        slettKravdatoSaksopplysninger(behandlingId, txSession)
        lagreKravdatoSaksopplysning(
            behandlingId = behandlingId,
            kravdatoSaksopplysning = kravdatoSaksopplysninger.kravdatoSaksopplysningFraSøknad!!,
            erAvklart = kravdatoSaksopplysninger.erOpplysningFraSøknadAvklart(),
            txSession = txSession,
        )
        if (kravdatoSaksopplysninger.harOpplysningFraSaksbehandler()) {
            lagreKravdatoSaksopplysning(
                behandlingId = behandlingId,
                kravdatoSaksopplysning = kravdatoSaksopplysninger.kravdatoSaksopplysningFraSaksbehandler!!,
                erAvklart = kravdatoSaksopplysninger.erOpplysningFraSaksbehandlerAvklart(),
                txSession = txSession,
            )
        }
    }

    fun lagreKravdatoSaksopplysning(
        behandlingId: BehandlingId,
        kravdatoSaksopplysning: KravdatoSaksopplysning,
        erAvklart: Boolean,
        txSession: TransactionalSession,
    ) {
        lagreKravdatoSaksopplysning(
            avklart = erAvklart,
            behandlingId = behandlingId,
            kravdatoSaksopplysning = kravdatoSaksopplysning,
            txSession = txSession,
        )
    }

    fun lagreKravdatoSaksopplysning(avklart: Boolean, kravdatoSaksopplysning: KravdatoSaksopplysning, behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreKravdatoSaksopplysningSql,
                mapOf(
                    "id" to UlidBase.random(ULID_PREFIX_KRAVDATO_SAKSOPPLYSNING).toString(),
                    "kravdato" to kravdatoSaksopplysning.kravdato,
                    "behandlingId" to behandlingId.toString(),
                    "avklartTidspunkt" to if (avklart) LocalDateTime.now() else null,
                    "saksbehandler" to kravdatoSaksopplysning.saksbehandlerIdent,
                    "datakilde" to kravdatoSaksopplysning.kilde.toString(),
                ),
            ).asUpdate,
        )
    }

    fun slettKravdatoSaksopplysninger(behandlingId: BehandlingId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettKravdatoSaksopplysningerSql, behandlingId.toString()).asUpdate)

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

    @Language("SQL")
    private val sqlHentKravdatoFraKilde = """
        select * from kravdato_saksopplysning where behandling_id = :behandlingId and datakilde = :kilde
    """.trimIndent()

    @Language("SQL")
    private val sqlHentAvklartKravdato = """
        select * from kravdato_saksopplysning where behandling_id = :behandlingId and avklart_tidspunkt is not null
    """.trimIndent()

    @Language("SQL")
    private val slettKravdatoSaksopplysningerSql = "delete from kravdato_saksopplysning where behandling_id = ?"

    @Language("SQL")
    private val lagreKravdatoSaksopplysningSql = """
        insert into kravdato_saksopplysning (
            id,
            kravdato,
            behandling_id,
            avklart_tidspunkt,
            saksbehandler,
            datakilde
        ) values (
            :id,
            :kravdato,
            :behandlingId,
            :avklartTidspunkt,
            :saksbehandler,
            :datakilde
        )
    """.trimIndent()

    companion object {
        private const val ULID_PREFIX_KRAVDATO_SAKSOPPLYSNING = "kravd"
    }
}
