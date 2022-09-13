package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.db.DataSource
import org.intellij.lang.annotations.Language
import java.util.*

internal class BarnetilleggRepo {


    fun lagre(søknadId: UUID, barnetillegg: List<Barnetillegg>) {
        slettBarnetillegg(søknadId)
        barnetillegg.forEach {
            lagreBarnetillegg(søknadId, it)
        }
    }

    fun hentBarnetilleggListe(søknadId: UUID): List<Barnetillegg> {
        return DataSource.session.run(
            queryOf(hentBarnetillegg, søknadId)
                .map { row ->
                    row.toBarnetillegg()
                }.asList
        )
    }

    private fun lagreBarnetillegg(søknadId: UUID, barnetillegg: Barnetillegg) {
        DataSource.session.run(
            queryOf(
                lagreBarnetillegg, mapOf(
                    "id" to UUID.randomUUID(),
                    "soknadId" to søknadId,
                    "ident" to barnetillegg.ident,
                    "fornavn" to barnetillegg.fornavn,
                    "etternavn" to barnetillegg.etternavn,
                    "alder" to barnetillegg.alder,
                    "land" to barnetillegg.land,
                )
            ).asUpdate
        )
    }

    private fun slettBarnetillegg(søknadId: UUID): Unit {
        DataSource.session.run(
            queryOf(slettBarnetillegg, søknadId).asUpdate
        )
    }

    private fun Row.toBarnetillegg(): Barnetillegg {
        val fornavn = stringOrNull("fornavn")
        val etternavn = stringOrNull("etternavn")
        val alder = int("alder")
        val ident = string("ident")
        val land = string("land")

        return Barnetillegg(
            fornavn = fornavn,
            etternavn = etternavn,
            alder = alder,
            ident = ident,
            land = land
        )
    }

    @Language("SQL")
    private val lagreBarnetillegg = """
        insert into barnetillegg (
            id,
            søknad_id,
            ident,
            fornavn, 
            etternavn, 
            alder,
            land
        ) values (
            :id,
            :soknadId,
            :ident,
            :fornavn, 
            :etternavn,
            :alder,
            :land
        )""".trimIndent()

    @Language("SQL")
    private val slettBarnetillegg = "delete from barnetillegg where søknad_id = ?"

    @Language("SQL")
    private val hentBarnetillegg = "select * from barnetillegg where søknad_id = ?"


}
