package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language
import java.util.*

internal class TiltakDAO {

    fun hent(søknadId: UUID, txSession: TransactionalSession): Tiltak =
        hentArenaTiltak(søknadId, txSession) ?: hentBrukerTiltak(søknadId, txSession)!!

    private fun hentArenaTiltak(søknadId: UUID, txSession: TransactionalSession): Tiltak.ArenaTiltak? {
        return txSession.run(
            queryOf(hentArenaTiltak, søknadId).map { row ->
                row.toArenatiltak()
            }.asSingle
        )
    }

    private fun hentBrukerTiltak(søknadId: UUID, txSession: TransactionalSession): Tiltak.BrukerregistrertTiltak? {
        return txSession.run(
            queryOf(hentBrukerregistrertTiltak, søknadId).map { row ->
                row.toBrukertiltak()
            }.asSingle
        )
    }

    fun lagre(søknadId: UUID, tiltak: Tiltak, txSession: TransactionalSession) {
        slettArenatiltak(søknadId, txSession)
        slettBrukertiltak(søknadId, txSession)

        when (tiltak) {
            is Tiltak.ArenaTiltak -> lagreArenatiltak(søknadId, tiltak, txSession)
            is Tiltak.BrukerregistrertTiltak -> lagreBrukertiltak(søknadId, tiltak, txSession)
        }
    }

    private fun lagreArenatiltak(søknadId: UUID, arenaTiltak: Tiltak.ArenaTiltak?, txSession: TransactionalSession) {
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

    private fun lagreBrukertiltak(
        søknadId: UUID,
        brukerregistrertTiltak: Tiltak.BrukerregistrertTiltak?,
        txSession: TransactionalSession
    ) {
        if (brukerregistrertTiltak != null) {
            txSession.run(
                queryOf(
                    lagreBrukerTiltak, mapOf(
                        "id" to UUID.randomUUID(),
                        "soknadId" to søknadId,
                        "tiltakstype" to brukerregistrertTiltak.tiltakskode?.name,    // TODO skal denne endre navn i basen ?
                        "arrangoernavn" to brukerregistrertTiltak.arrangoernavn,
                        "beskrivelse" to brukerregistrertTiltak.beskrivelse,
                        "fom" to brukerregistrertTiltak.fom,
                        "tom" to brukerregistrertTiltak.tom,
                        "adresse" to brukerregistrertTiltak.adresse,
                        "postnummer" to brukerregistrertTiltak.postnummer,
                        "antallDager" to brukerregistrertTiltak.antallDager,
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

    private fun slettBrukertiltak(søknadId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettBrukerregistrertTiltak, søknadId).asUpdate
        )
    }

    private fun Row.toArenatiltak(): Tiltak.ArenaTiltak {
        val arenaId = stringOrNull("arena_id")
        val arrangoer = stringOrNull("arrangoer")
        val harSluttdatoFraArena = boolean("har_sluttdato_fra_arena")
        val navn = stringOrNull("navn")
        val erIEndreStatus = boolean("er_i_endre_status")
        val opprinneligStartdato = localDateOrNull("opprinnelig_startdato")
        val opprinneligSluttdato = localDateOrNull("opprinnelig_sluttdato")
        val startdato = localDateOrNull("startdato")
        val sluttdato = localDateOrNull("sluttdato")
        return Tiltak.ArenaTiltak(
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

    private fun Row.toBrukertiltak(): Tiltak.BrukerregistrertTiltak {
        val tiltakskode = stringOrNull("tiltakstype")?.let { Tiltaksaktivitet.Tiltak.valueOf(it) }
        val arrangoernavn = stringOrNull("arrangoernavn")
        val beskrivelse = stringOrNull("beskrivelse")
        val fom = localDateOrNull("fom")
        val tom = localDateOrNull("tom")
        val adresse = stringOrNull("adresse")
        val postnummer = stringOrNull("postnummer")
        val antallDager = int("antall_dager")

        return Tiltak.BrukerregistrertTiltak(
            tiltakskode = tiltakskode,
            arrangoernavn = arrangoernavn,
            beskrivelse = beskrivelse,
            fom = fom,
            tom = tom,
            adresse = adresse,
            postnummer = postnummer,
            antallDager = antallDager,
        )
    }

    @Language("SQL")
    private val hentBrukerregistrertTiltak = "select * from brukertiltak where søknad_id = ?"

    @Language("SQL")
    private val hentArenaTiltak = "select * from arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val slettBrukerregistrertTiltak = "delete from brukertiltak where søknad_id = ?"

    @Language("SQL")
    private val slettArenaTiltak = "delete from arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val lagreBrukerTiltak = """
        insert into brukertiltak (
            id,
            søknad_id,
            tiltakstype,
            arrangoernavn, 
            beskrivelse, 
            fom,
            tom,
            adresse,
            postnummer,
            antall_dager
        ) values (
            :id,
            :soknadId,
            :tiltakstype,
            :arrangoernavn, 
            :beskrivelse,
            :fom,
            :tom,
            :adresse,
            :postnummer,
            :antallDager
        )""".trimIndent()

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
