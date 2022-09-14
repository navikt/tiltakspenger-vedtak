package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language

internal class BrukertiltakDAO {

    fun hent(søknadId: UUID, txSession: TransactionalSession): BrukerregistrertTiltak? {
        return txSession.run(
            queryOf(hentBrukerregistrertTiltak, søknadId).map { row ->
                row.toBrukertiltak()
            }.asSingle
        )
    }

    fun lagre(søknadId: UUID, brukerregistrertTiltak: BrukerregistrertTiltak?, txSession: TransactionalSession) {
        slettBrukertiltak(søknadId, txSession)
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

    private fun slettBrukertiltak(søknadId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettBrukerregistrertTiltak, søknadId).asUpdate
        )
    }

    private fun Row.toBrukertiltak(): BrukerregistrertTiltak {
        val tiltakskode = stringOrNull("tiltakstype")?.let { Tiltaksaktivitet.Tiltak.valueOf(it) }
        val arrangoernavn = stringOrNull("arrangoernavn")
        val beskrivelse = stringOrNull("beskrivelse")
        val fom = localDateOrNull("fom")
        val tom = localDateOrNull("tom")
        val adresse = stringOrNull("adresse")
        val postnummer = stringOrNull("postnummer")
        val antallDager = int("antall_dager")

        return BrukerregistrertTiltak(
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
    private val slettBrukerregistrertTiltak = "delete from brukertiltak where søknad_id = ?"

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
}
