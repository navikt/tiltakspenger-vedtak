package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknadstiltak
import org.intellij.lang.annotations.Language

internal object SøknadTiltakDAO {

    private const val ULID_PREFIX_TILTAK = "tilt"

    fun hent(
        søknadId: SøknadId,
        session: Session,
    ): Søknadstiltak = hentTiltak(søknadId, session)!!

    private fun hentTiltak(
        søknadId: SøknadId,
        session: Session,
    ): Søknadstiltak? =
        session.run(
            queryOf(hentTiltak, søknadId.toString()).map { row -> row.toTiltak() }.asSingle,
        )

    fun lagre(
        søknadId: SøknadId,
        søknadstiltak: Søknadstiltak,
        txSession: TransactionalSession,
    ) {
        slettTiltak(søknadId, txSession)
        lagreTiltak(søknadId, søknadstiltak, txSession)
    }

    private fun lagreTiltak(
        søknadId: SøknadId,
        søknadstiltak: Søknadstiltak,
        session: Session,
    ) {
        session.run(
            queryOf(
                lagreTiltak,
                mapOf(
                    "id" to random(ULID_PREFIX_TILTAK).toString(),
                    "soknadId" to søknadId.toString(),
                    "eksternId" to søknadstiltak.id,
                    "arrangornavn" to søknadstiltak.arrangør,
                    "typekode" to søknadstiltak.typeKode,
                    "typenavn" to søknadstiltak.typeNavn,
                    "deltakelseFom" to søknadstiltak.deltakelseFom,
                    "deltakelseTom" to søknadstiltak.deltakelseTom,
                ),
            ).asUpdate,
        )
    }

    private fun slettTiltak(
        søknadId: SøknadId,
        session: Session,
    ) {
        session.run(queryOf(slettTiltak, søknadId.toString()).asUpdate)
    }

    private fun Row.toTiltak(): Søknadstiltak {
        val eksternId = string("ekstern_id")
        val arrangørnavn = string("arrangørnavn")
        val typekode = string("typekode")
        val typenavn = string("typenavn")
        val deltakelseFom = localDate("deltakelse_fom")
        val deltakelseTom = localDate("deltakelse_tom")
        return Søknadstiltak(
            id = eksternId,
            deltakelseFom = deltakelseFom,
            deltakelseTom = deltakelseTom,
            arrangør = arrangørnavn,
            typeKode = typekode,
            typeNavn = typenavn,
        )
    }

    @Language("SQL")
    private val hentTiltak = "select * from søknadstiltak where søknad_id = ?"

    @Language("SQL")
    private val slettTiltak = "delete from søknadstiltak where søknad_id = ?"

    @Language("SQL")
    private val lagreTiltak =
        """
        insert into søknadstiltak (
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
}
