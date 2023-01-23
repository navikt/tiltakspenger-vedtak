package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language

internal class TiltakDAO {

    fun hent(søknadId: SøknadId, txSession: TransactionalSession): Tiltak? =
        hentArenaTiltak(søknadId, txSession) ?: hentBrukerTiltak(søknadId, txSession)

    private fun hentArenaTiltak(søknadId: SøknadId, txSession: TransactionalSession): Tiltak.ArenaTiltak? {
        return txSession.run(
            queryOf(hentArenaTiltak, søknadId.toString()).map { row -> row.toArenatiltak() }.asSingle
        )
    }

    private fun hentBrukerTiltak(søknadId: SøknadId, txSession: TransactionalSession): Tiltak.BrukerregistrertTiltak? {
        return txSession.run(
            queryOf(hentBrukerregistrertTiltak, søknadId.toString()).map { row -> row.toBrukertiltak() }.asSingle
        )
    }

    fun lagre(søknadId: SøknadId, tiltak: Tiltak?, txSession: TransactionalSession) {
        slettArenatiltak(søknadId, txSession)
        slettBrukertiltak(søknadId, txSession)

        when (tiltak) {
            is Tiltak.ArenaTiltak -> lagreArenatiltak(søknadId, tiltak, txSession)
            is Tiltak.BrukerregistrertTiltak -> lagreBrukertiltak(søknadId, tiltak, txSession)
            else -> {}
        }
    }

    private fun lagreArenatiltak(
        søknadId: SøknadId,
        arenaTiltak: Tiltak.ArenaTiltak?,
        txSession: TransactionalSession
    ) {
        if (arenaTiltak != null) {
            txSession.run(
                queryOf(
                    lagreArenaTiltak,
                    mapOf(
                        "id" to random(ULID_PREFIX_ARENATILTAK).toString(),
                        "soknadId" to søknadId.toString(),
                        "arenaId" to arenaTiltak.arenaId,
                        "arrangoernavn" to arenaTiltak.arrangoernavn,
                        "harSluttdatoFraArena" to arenaTiltak.harSluttdatoFraArena,
                        "tiltakskode" to arenaTiltak.tiltakskode.name,
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
        søknadId: SøknadId,
        brukerregistrertTiltak: Tiltak.BrukerregistrertTiltak?,
        txSession: TransactionalSession
    ) {
        if (brukerregistrertTiltak != null) {
            txSession.run(
                queryOf(
                    lagreBrukerTiltak,
                    mapOf(
                        "id" to random(ULID_PREFIX_BRUKERTILTAK).toString(),
                        "soknadId" to søknadId.toString(),
                        "tiltakskode" to brukerregistrertTiltak.tiltakskode?.name,
                        "arrangoernavn" to brukerregistrertTiltak.arrangoernavn,
                        "beskrivelse" to brukerregistrertTiltak.beskrivelse,
                        "startdato" to brukerregistrertTiltak.startdato,
                        "sluttdato" to brukerregistrertTiltak.sluttdato,
                        "adresse" to brukerregistrertTiltak.adresse,
                        "postnummer" to brukerregistrertTiltak.postnummer,
                        "antallDager" to brukerregistrertTiltak.antallDager,
                    )
                ).asUpdate
            )
        }
    }

    private fun slettArenatiltak(søknadId: SøknadId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettArenaTiltak, søknadId.toString()).asUpdate)
    }

    private fun slettBrukertiltak(søknadId: SøknadId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettBrukerregistrertTiltak, søknadId.toString()).asUpdate)
    }

    private fun Row.toArenatiltak(): Tiltak.ArenaTiltak {
        val arenaId = string("arena_id")
        val arrangoernavn = stringOrNull("arrangoernavn")
        val harSluttdatoFraArena = boolean("har_sluttdato_fra_arena")
        val tiltakskode = string("tiltakskode")
        val erIEndreStatus = boolean("er_i_endre_status")
        val opprinneligStartdato = localDate("opprinnelig_startdato")
        val opprinneligSluttdato = localDateOrNull("opprinnelig_sluttdato")
        val startdato = localDate("startdato")
        val sluttdato = localDateOrNull("sluttdato")
        return Tiltak.ArenaTiltak(
            arenaId = arenaId,
            arrangoernavn = arrangoernavn,
            harSluttdatoFraArena = harSluttdatoFraArena,
            tiltakskode = tiltakskode.let { Tiltaksaktivitet.Tiltak.valueOf(it) },
            erIEndreStatus = erIEndreStatus,
            opprinneligSluttdato = opprinneligSluttdato,
            opprinneligStartdato = opprinneligStartdato,
            sluttdato = sluttdato,
            startdato = startdato
        )
    }

    private fun Row.toBrukertiltak(): Tiltak.BrukerregistrertTiltak {
        val tiltakskode = stringOrNull("tiltakskode")?.let { Tiltaksaktivitet.Tiltak.valueOf(it) }
        val arrangoernavn = stringOrNull("arrangoernavn")
        val beskrivelse = stringOrNull("beskrivelse")
        val fom = localDate("startdato")
        val tom = localDate("sluttdato")
        val adresse = stringOrNull("adresse")
        val postnummer = stringOrNull("postnummer")
        val antallDager = int("antall_dager")

        return Tiltak.BrukerregistrertTiltak(
            tiltakskode = tiltakskode,
            arrangoernavn = arrangoernavn,
            beskrivelse = beskrivelse,
            startdato = fom,
            sluttdato = tom,
            adresse = adresse,
            postnummer = postnummer,
            antallDager = antallDager,
        )
    }

    @Language("SQL")
    private val hentBrukerregistrertTiltak = "select * from søknad_brukertiltak where søknad_id = ?"

    @Language("SQL")
    private val hentArenaTiltak = "select * from søknad_arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val slettBrukerregistrertTiltak = "delete from søknad_brukertiltak where søknad_id = ?"

    @Language("SQL")
    private val slettArenaTiltak = "delete from søknad_arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val lagreBrukerTiltak = """
        insert into søknad_brukertiltak (
            id,
            søknad_id,
            tiltakskode,
            arrangoernavn, 
            beskrivelse, 
            startdato,
            sluttdato,
            adresse,
            postnummer,
            antall_dager
        ) values (
            :id,
            :soknadId,
            :tiltakskode,
            :arrangoernavn, 
            :beskrivelse,
            :startdato,
            :sluttdato,
            :adresse,
            :postnummer,
            :antallDager
        )
    """.trimIndent()

    @Language("SQL")
    private val lagreArenaTiltak = """
        insert into søknad_arenatiltak (
            id,
            søknad_id,
            arena_id,
            arrangoernavn, 
            har_sluttdato_fra_arena, 
            tiltakskode,
            er_i_endre_status,
            opprinnelig_startdato,
            opprinnelig_sluttdato,
            startdato,
            sluttdato
        ) values (
            :id,
            :soknadId,
            :arenaId,
            :arrangoernavn, 
            :harSluttdatoFraArena,
            :tiltakskode,
            :erIEndreStatus,
            :opprinneligStartdato,
            :opprinneligSluttdato,
            :startdato,
            :sluttdato
        )
    """.trimIndent()

    companion object {
        private const val ULID_PREFIX_ARENATILTAK = "atilt"
        private const val ULID_PREFIX_BRUKERTILTAK = "btilt"
    }
}
