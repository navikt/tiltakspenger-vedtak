package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
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
                "opphold_i_eos" to barnetillegg.oppholderSegIEØS,
            )

            is Barnetillegg.Manuell -> mapOf(
                "type" to "MANUELL",
                "id" to random(ULID_PREFIX_BARNETILLEGG).toString(),
                "soknadId" to søknadId.toString(),
                "fodselsdato" to barnetillegg.fødselsdato,
                "fornavn" to barnetillegg.fornavn,
                "mellomnavn" to barnetillegg.mellomnavn,
                "etternavn" to barnetillegg.etternavn,
                "opphold_i_eos" to barnetillegg.oppholderSegIEØS,
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
        val fødselsdato = localDate("fødselsdato")
        val fornavn = string("fornavn")
        val mellomnavn = stringOrNull("mellomnavn")
        val etternavn = string("etternavn")
        val oppholderSegIEØS = booleanOrNull("opphold_i_eos")
        //TODO: Bør lagre som et skikkelig JaNeiSpm, som i Søknad!
        return if (type == "PDL") {
            Barnetillegg.FraPdl(
                oppholderSegIEØS = oppholderSegIEØS?.let { if (it) Søknad.JaNeiSpm.Ja else Søknad.JaNeiSpm.Nei }
                    ?: Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                fødselsdato = fødselsdato,
            )
        } else {
            Barnetillegg.Manuell(
                oppholderSegIEØS = oppholderSegIEØS?.let { if (it) Søknad.JaNeiSpm.Ja else Søknad.JaNeiSpm.Nei }
                    ?: Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
                fødselsdato = fødselsdato,
            )
        }
    }

    @Language("SQL")
    private val lagreBarnetillegg = """
        insert into søknad_barnetillegg (
            id,
            søknad_id,
            fødselsdato,
            fornavn,
            mellomnavn,
            etternavn,
            opphold_i_eos
        ) values (
            :id,
            :soknadId,
            :fodselsdato,
            :fornavn,
            :mellomnavn,
            :etternavn,
            :opphold_i_eos
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
