package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Barnetillegg
import org.intellij.lang.annotations.Language

internal class BarnetilleggDAO {

    fun lagre(søknadId: SøknadId, barnetillegg: List<Barnetillegg>, txSession: TransactionalSession) {
        slettBarnetillegg(søknadId, txSession)
        barnetillegg.forEach {
            lagreBarnetillegg(søknadId, it, txSession)
        }
    }

    fun hentBarnetilleggListe(søknadId: SøknadId, txSession: TransactionalSession): List<Barnetillegg> {
        return txSession.run(
            queryOf(hentBarnetillegg, søknadId.toString())
                .map { row -> row.toBarnetillegg() }
                .asList,
        )
    }

    private fun lagreBarnetillegg(søknadId: SøknadId, barnetillegg: Barnetillegg, txSession: TransactionalSession) {
        val paramMap = when (barnetillegg) {
            is Barnetillegg.MedIdent -> mapOf(
                "id" to random(ULID_PREFIX_BARNETILLEGG).toString(),
                "soknadId" to søknadId.toString(),
                "ident" to barnetillegg.ident,
                "fodselsdato" to null,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "alder" to barnetillegg.alder,
                "oppholdsland" to barnetillegg.oppholdsland,
                "soktBarnetillegg" to barnetillegg.søktBarnetillegg,
            )

            is Barnetillegg.UtenIdent -> mapOf(
                "id" to random(ULID_PREFIX_BARNETILLEGG).toString(),
                "soknadId" to søknadId.toString(),
                "ident" to null,
                "fodselsdato" to barnetillegg.fødselsdato,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "alder" to barnetillegg.alder,
                "oppholdsland" to barnetillegg.oppholdsland,
                "soktBarnetillegg" to barnetillegg.søktBarnetillegg,
            )
        }
        txSession.run(
            queryOf(lagreBarnetillegg, paramMap).asUpdate,
        )
    }

    private fun slettBarnetillegg(søknadId: SøknadId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettBarnetillegg, søknadId.toString()).asUpdate,
        )
    }

    private fun Row.toBarnetillegg(): Barnetillegg {
        val ident = stringOrNull("ident")
        val fødselsdato = localDateOrNull("fødselsdato")
        val alder = int("alder")
        val oppholdsland = string("oppholdsland")
        val fornavn = stringOrNull("fornavn")
        val mellomnavn = stringOrNull("mellomnavn")
        val etternavn = stringOrNull("etternavn")
        val søktBarnetillegg = boolean("søkt_barnetillegg")
        return if (ident != null) {
            Barnetillegg.MedIdent(
                alder = alder,
                oppholdsland = oppholdsland,
                ident = ident,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                søktBarnetillegg = søktBarnetillegg,
            )
        } else {
            Barnetillegg.UtenIdent(
                alder = alder,
                oppholdsland = oppholdsland,
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
        insert into søknad_barnetillegg (
            id,
            søknad_id,
            ident,
            fødselsdato,
            fornavn,
            mellomnavn,
            etternavn,
            alder,
            oppholdsland,
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
            :oppholdsland,
            :soktBarnetillegg
        )
    """.trimIndent()

    @Language("SQL")
    private val slettBarnetillegg = "delete from søknad_barnetillegg where søknad_id = ?"

    @Language("SQL")
    private val hentBarnetillegg = "select * from søknad_barnetillegg where søknad_id = ?"

    companion object {
        private const val ULID_PREFIX_BARNETILLEGG = "btil"
    }
}
