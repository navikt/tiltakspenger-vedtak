package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Barnetillegg
import org.intellij.lang.annotations.Language
import java.util.*

internal class BarnetilleggDAO {


    fun lagre(søknadId: UUID, barnetillegg: List<Barnetillegg>, txSession: TransactionalSession) {
        slettBarnetillegg(søknadId, txSession)
        barnetillegg.forEach {
            lagreBarnetillegg(søknadId, it, txSession)
        }
    }

    fun hentBarnetilleggListe(søknadId: UUID, txSession: TransactionalSession): List<Barnetillegg> {
        return txSession.run(
            queryOf(hentBarnetillegg, søknadId)
                .map { row ->
                    row.toBarnetillegg()
                }.asList
        )
    }

    private fun lagreBarnetillegg(søknadId: UUID, barnetillegg: Barnetillegg, txSession: TransactionalSession) {
        val paramMap = when (barnetillegg) {
            is Barnetillegg.MedIdent -> mapOf(
                "id" to UUID.randomUUID(),
                "soknadId" to søknadId,
                "ident" to barnetillegg.ident,
                "fodselsdato" to null,
                "alder" to barnetillegg.alder,
                "land" to barnetillegg.land,
            )

            is Barnetillegg.UtenIdent -> mapOf(
                "id" to UUID.randomUUID(),
                "soknadId" to søknadId,
                "ident" to null,
                "fodselsdato" to barnetillegg.fødselsdato,
                "alder" to barnetillegg.alder,
                "land" to barnetillegg.land,
            )
        }
        txSession.run(
            queryOf(lagreBarnetillegg, paramMap).asUpdate
        )
    }

    private fun slettBarnetillegg(søknadId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettBarnetillegg, søknadId).asUpdate
        )
    }

    private fun Row.toBarnetillegg(): Barnetillegg {
        val ident = stringOrNull("ident")
        val fødselsdato = localDateOrNull("fødselsdato")
        val alder = int("alder")
        val land = string("land")
        return if (ident != null) {
            Barnetillegg.MedIdent(alder = alder, land = land, ident = ident)
        } else {
            Barnetillegg.UtenIdent(alder = alder, land = land, fødselsdato = fødselsdato!!)
        }
    }

    @Language("SQL")
    private val lagreBarnetillegg = """
        insert into barnetillegg (
            id,
            søknad_id,
            ident,
            fødselsdato,
            alder,
            land
        ) values (
            :id,
            :soknadId,
            :ident,
            :fodselsdato,
            :alder,
            :land
        )""".trimIndent()

    @Language("SQL")
    private val slettBarnetillegg = "delete from barnetillegg where søknad_id = ?"

    @Language("SQL")
    private val hentBarnetillegg = "select * from barnetillegg where søknad_id = ?"


}
