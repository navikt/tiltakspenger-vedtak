package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language

internal class ArenatiltakDAO {

    fun hent(søknadId: UUID, txSession: TransactionalSession): ArenaTiltak? {
        return txSession.run(
            queryOf(hentArenaTiltak, søknadId).map { row ->
                row.toArenatiltak()
            }.asSingle
        )
    }

    fun lagre(søknadId: UUID, arenaTiltak: ArenaTiltak?, txSession: TransactionalSession) {
        slettArenatiltak(søknadId, txSession)
        if (arenaTiltak != null) {
            txSession.run(
                queryOf(
                    lagreArenaTiltak, mapOf(
                        "id" to UUID.randomUUID(),
                        "soknadId" to søknadId,
                        "arenaId" to arenaTiltak.arenaId,
                        "arrangoer" to arenaTiltak.arrangoer,
                        "harSluttdatoFraArena" to arenaTiltak.harSluttdatoFraArena,
                        "navn" to arenaTiltak.tiltakskode?.name,                  // TODO sjekk om denne er riktig, og om den skal endre navn i basen
                        "erIEndreStatus" to arenaTiltak.erIEndreStatus,
                        "opprinneligStartdato" to arenaTiltak.opprinneligStartdato,
                        "opprinneligSluttdato" to arenaTiltak.opprinneligSluttdato,
                        "startdato" to arenaTiltak.startdato,
                        "sluttdato" to arenaTiltak.sluttdato,
                    )
                ).asUpdate
            )
        }
    }

    private fun slettArenatiltak(søknadId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettArenaTiltak, søknadId).asUpdate
        )
    }

    private fun Row.toArenatiltak(): ArenaTiltak {
        val arenaId = stringOrNull("arena_id")
        val arrangoer = stringOrNull("arrangoer")
        val harSluttdatoFraArena = boolean("har_sluttdato_fra_arena")
        val navn = stringOrNull("navn")
        val erIEndreStatus = boolean("er_i_endre_status")
        val opprinneligStartdato = localDateOrNull("opprinnelig_startdato")
        val opprinneligSluttdato = localDateOrNull("opprinnelig_sluttdato")
        val startdato = localDateOrNull("startdato")
        val sluttdato = localDateOrNull("sluttdato")
        return ArenaTiltak(
            arenaId = arenaId,
            arrangoer = arrangoer,
            harSluttdatoFraArena = harSluttdatoFraArena,
            tiltakskode = navn?.let { Tiltaksaktivitet.Tiltak.valueOf(it) },
            erIEndreStatus = erIEndreStatus,
            opprinneligSluttdato = opprinneligSluttdato,
            opprinneligStartdato = opprinneligStartdato,
            sluttdato = sluttdato,
            startdato = startdato
        )
    }

    @Language("SQL")
    private val hentArenaTiltak = "select * from arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val slettArenaTiltak = "delete from arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val lagreArenaTiltak = """
        insert into arenatiltak (
            id,
            søknad_id,
            arena_id,
            arrangoer, 
            har_sluttdato_fra_arena, 
            navn,
            er_i_endre_status,
            opprinnelig_startdato,
            opprinnelig_sluttdato,
            startdato,
            sluttdato
        ) values (
            :id,
            :soknadId,
            :arenaId,
            :arrangoer, 
            :harSluttdatoFraArena,
            :navn,
            :erIEndreStatus,
            :opprinneligStartdato,
            :opprinneligSluttdato,
            :startdato,
            :sluttdato
        )""".trimIndent()
}
