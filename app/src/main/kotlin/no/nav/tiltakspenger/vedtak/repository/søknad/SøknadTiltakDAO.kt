package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.behandling.SøknadsTiltak
import org.intellij.lang.annotations.Language

internal class SøknadTiltakDAO {

    fun hent(søknadId: SøknadId, session: Session): SøknadsTiltak =
        hentTiltak(søknadId, session)!!

    private fun hentTiltak(søknadId: SøknadId, session: Session): SøknadsTiltak? {
        return session.run(
            queryOf(hentTiltak, søknadId.toString()).map { row -> row.toTiltak() }.asSingle,
        )
    }

    fun lagre(søknadId: SøknadId, tiltak: SøknadsTiltak, txSession: TransactionalSession) {
        slettTiltak(søknadId, txSession)
        lagreTiltak(søknadId, tiltak, txSession)
    }

    private fun lagreTiltak(
        søknadId: SøknadId,
        tiltak: SøknadsTiltak,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagreTiltak,
                mapOf(
                    "id" to random(ULID_PREFIX_TILTAK).toString(),
                    "soknadId" to søknadId.toString(),
                    "eksternId" to tiltak.id,
                    "arrangornavn" to tiltak.arrangør,
                    "typekode" to tiltak.typeKode,
                    "typenavn" to tiltak.typeNavn,
                    "deltakelseFom" to tiltak.deltakelseFom,
                    "deltakelseTom" to tiltak.deltakelseTom,
                ),
            ).asUpdate,
        )
    }

    private fun slettTiltak(søknadId: SøknadId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettTiltak, søknadId.toString()).asUpdate)
    }

    private fun Row.toTiltak(): SøknadsTiltak {
        val eksternId = string("ekstern_id")
        val arrangørnavn = string("arrangørnavn")
        val typekode = string("typekode")
        val typenavn = string("typenavn")
        val deltakelseFom = localDate("deltakelse_fom")
        val deltakelseTom = localDate("deltakelse_tom")
        return SøknadsTiltak(
            id = eksternId,
            deltakelseFom = deltakelseFom,
            deltakelseTom = deltakelseTom,
            arrangør = arrangørnavn,
            typeKode = typekode,
            typeNavn = typenavn,
        )
    }

    @Language("SQL")
    private val hentTiltak = "select * from søknad_tiltak where søknad_id = ?"

    @Language("SQL")
    private val slettTiltak = "delete from søknad_tiltak where søknad_id = ?"

    @Language("SQL")
    private val lagreTiltak = """
        insert into søknad_tiltak (
            id,
            søknad_id,
            ekstern_id,
            arrangørnavn, 
            typekode,
            typenavn,
            deltakelse_fom,
            deltakelse_tom
        ) values (
            :id,
            :soknadId,
            :eksternId,
            :arrangornavn, 
            :typekode,
            :typenavn,
            :deltakelseFom,
            :deltakelseTom
        )
    """.trimIndent()

    companion object {
        private const val ULID_PREFIX_TILTAK = "tilt"
    }
}
