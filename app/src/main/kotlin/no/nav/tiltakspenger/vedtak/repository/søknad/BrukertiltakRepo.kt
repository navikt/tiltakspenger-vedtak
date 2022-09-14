package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import org.intellij.lang.annotations.Language
import java.util.*

internal class BrukertiltakRepo {

    fun hent(søknadId: UUID) : BrukerregistrertTiltak? {
        return session.run(
            queryOf(hentBrukerregistrertTiltak, søknadId).map { row ->
                row.toBrukertiltak()
            }.asSingle
        )
    }

    fun lagre(søknadId: UUID, brukerregistrertTiltak: BrukerregistrertTiltak?) : Int {
        slettBrukertiltak(søknadId)
        if (brukerregistrertTiltak != null) {
            session.run(
                queryOf(
                    lagreBrukerTiltak, mapOf(
                        "id" to UUID.randomUUID(),
                        "soknadId" to søknadId,
                        "tiltakstype" to brukerregistrertTiltak.tiltakskode,    // TODO skal denne endre navn i basen ?
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
            return 1
        } else return 0
    }

    private fun slettBrukertiltak(søknadId: UUID): Unit {
        session.run(
            queryOf(slettBrukerregistrertTiltak, søknadId).asUpdate
        )
    }

    private fun Row.toBrukertiltak() : BrukerregistrertTiltak {
        val tiltakstype = stringOrNull("tiltakstype")
        val tiltakskode = if (tiltakstype == null) null else Tiltaksaktivitet.Tiltak.valueOf(tiltakstype)
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
