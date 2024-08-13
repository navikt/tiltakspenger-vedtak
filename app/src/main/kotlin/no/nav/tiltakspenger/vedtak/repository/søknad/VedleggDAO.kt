package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Vedlegg
import org.intellij.lang.annotations.Language

internal class VedleggDAO {
    fun lagre(
        søknadId: SøknadId,
        vedlegg: List<Vedlegg>?,
        txSession: TransactionalSession,
    ) {
        slett(søknadId, txSession)
        vedlegg?.forEach {
            lagreVedlegg(søknadId, it, txSession)
        }
    }

    fun hentVedleggListe(
        søknadId: SøknadId,
        session: Session,
    ): List<Vedlegg> =
        session.run(
            queryOf(hentVedlegg, søknadId.toString())
                .map { row -> row.toVedlegg() }
                .asList,
        )

    private fun lagreVedlegg(
        søknadId: SøknadId,
        vedlegg: Vedlegg,
        session: Session,
    ) {
        session.run(
            queryOf(
                lagreVedlegg,
                mapOf(
                    "id" to random(ULID_PREFIX_VEDLEGG).toString(),
                    "soknadId" to søknadId.toString(),
                    "journalpostId" to vedlegg.journalpostId,
                    "dokumentInfoId" to vedlegg.dokumentInfoId,
                    "filnavn" to vedlegg.filnavn,
                ),
            ).asUpdate,
        )
    }

    private fun slett(
        søknadId: SøknadId,
        session: Session,
    ) {
        session.run(
            queryOf(slettVedlegg, søknadId.toString()).asUpdate,
        )
    }

    private fun Row.toVedlegg(): Vedlegg {
        val journalpostId = string("journalpost_id")
        val dokumentInfoId = string("dokumentinfo_id")
        val filnavn = stringOrNull("filnavn")

        return Vedlegg(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = filnavn,
        )
    }

    @Language("SQL")
    private val hentVedlegg = "select * from søknad_vedlegg where søknad_id = ?"

    @Language("SQL")
    private val slettVedlegg = "delete from søknad_vedlegg where søknad_id = ?"

    @Language("SQL")
    private val lagreVedlegg =
        """
        insert into søknad_vedlegg (
            id,
            søknad_id,
            journalpost_id,
            dokumentinfo_id,
            filnavn
        ) values (
            :id,
            :soknadId,
            :journalpostId,
            :dokumentInfoId,
            :filnavn
        )
        """.trimIndent()

    companion object {
        private const val ULID_PREFIX_VEDLEGG = "vedlegg"
    }
}
