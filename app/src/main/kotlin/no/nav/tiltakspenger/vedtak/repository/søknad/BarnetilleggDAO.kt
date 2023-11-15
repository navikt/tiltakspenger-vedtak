package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.domene.behandling.Barnetillegg
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
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
            is Barnetillegg.FraPdl -> mapOf(
                "type" to "PDL",
                "id" to random(ULID_PREFIX_BARNETILLEGG).toString(),
                "soknadId" to søknadId.toString(),
                "fodselsdato" to barnetillegg.fødselsdato,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "opphold_i_eos_type" to lagreJaNeiSpmType(barnetillegg.oppholderSegIEØS),
            )

            is Barnetillegg.Manuell -> mapOf(
                "type" to "MANUELL",
                "id" to random(ULID_PREFIX_BARNETILLEGG).toString(),
                "soknadId" to søknadId.toString(),
                "fodselsdato" to barnetillegg.fødselsdato,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "opphold_i_eos_type" to lagreJaNeiSpmType(barnetillegg.oppholderSegIEØS),
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
        val type = string("type")
        val fødselsdato = localDate("fodselsdato")
        val fornavn = stringOrNull("fornavn")
        val mellomnavn = stringOrNull("mellomnavn")
        val etternavn = stringOrNull("etternavn")
        val oppholderSegIEØS = jaNeiSpm("opphold_i_eos")
        return if (type == "PDL") {
            Barnetillegg.FraPdl(
                oppholderSegIEØS = oppholderSegIEØS,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                fødselsdato = fødselsdato,
            )
        } else {
            Barnetillegg.Manuell(
                oppholderSegIEØS = oppholderSegIEØS,
                fornavn = fornavn!!,
                mellomnavn = mellomnavn,
                etternavn = etternavn!!,
                fødselsdato = fødselsdato,
            )
        }
    }

    @Language("SQL")
    private val lagreBarnetillegg = """
        insert into søknad_barnetillegg (
            id,
            søknad_id,
            type,
            fodselsdato,
            fornavn,
            mellomnavn,
            etternavn,
            opphold_i_eos_type
        ) values (
            :id,
            :soknadId,
            :type,
            :fodselsdato,
            :fornavn,
            :mellomnavn,
            :etternavn,
            :opphold_i_eos_type
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
