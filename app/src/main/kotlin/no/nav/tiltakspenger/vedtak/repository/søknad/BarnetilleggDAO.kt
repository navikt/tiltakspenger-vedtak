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
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "alder" to barnetillegg.alder,
                "land" to barnetillegg.land,
                "soktBarnetillegg" to barnetillegg.søktBarnetillegg,
            )

            is Barnetillegg.UtenIdent -> mapOf(
                "id" to UUID.randomUUID(),
                "soknadId" to søknadId,
                "ident" to null,
                "fodselsdato" to barnetillegg.fødselsdato,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "alder" to barnetillegg.alder,
                "land" to barnetillegg.land,
                "soktBarnetillegg" to barnetillegg.søktBarnetillegg,
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
        val fornavn = stringOrNull("fornavn")
        val mellomnavn = stringOrNull("mellomnavn")
        val etternavn = stringOrNull("etternavn")
        val søktBarnetillegg = boolean("søkt_barnetillegg")
        return if (ident != null) {
            Barnetillegg.MedIdent(
                alder = alder,
                land = land,
                ident = ident,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                søktBarnetillegg = søktBarnetillegg,
            )
        } else {
            Barnetillegg.UtenIdent(
                alder = alder,
                land = land,
                fødselsdato = fødselsdato!!,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                søktBarnetillegg = søktBarnetillegg,
            )
        }
    }

    @Language("SQL")
    private val lagreBarnetillegg = """
        insert into barnetillegg (
            id,
            søknad_id,
            ident,
            fødselsdato,
            fornavn,
            mellomnavn,
            etternavn,
            alder,
            land,
            søkt_barnetillegg
        ) values (
            :id,
            :soknadId,
            :ident,
            :fodselsdato,
            :fornavn,
            :mellomnavn,
            :etternavn,
            :alder,
            :land,
            :soktBarnetillegg
        )""".trimIndent()

    @Language("SQL")
    private val slettBarnetillegg = "delete from barnetillegg where søknad_id = ?"

    @Language("SQL")
    private val hentBarnetillegg = "select * from barnetillegg where søknad_id = ?"


}
