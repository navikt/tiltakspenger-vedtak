package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Personopplysninger
import org.intellij.lang.annotations.Language

internal class PersonopplysningerBarnUtenIdentDAO {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(innsendingId: InnsendingId, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, innsendingId.toString()).map(toPersonopplysninger).asList)

    internal fun lagre(
        innsendingId: InnsendingId,
        personopplysninger: Personopplysninger.BarnUtenIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn uten ident $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to random(ULID_PREFIX_BARN_UTEN_IDENT).toString(),
                    "innsendingId" to innsendingId.toString(),
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    internal fun slett(innsendingId: InnsendingId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, innsendingId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> Personopplysninger.BarnUtenIdent = { row ->
        Personopplysninger.BarnUtenIdent(
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss")
        )
    }

    @Language("SQL")
    private val slettSql = "delete from personopplysninger_barn_uten_ident where innsending_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger_barn_uten_ident where innsending_id = ?"

    @Language("SQL")
    private val lagreSql = """
        insert into personopplysninger_barn_uten_ident (
            id,
            innsending_id,        
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            tidsstempel_hos_oss            
        ) values (
            :id,
            :innsendingId,
            :fodselsdato,   
            :fornavn,           
            :mellomnavn,        
            :etternavn,         
            :tidsstempelHosOss
        )
    """.trimIndent()

    companion object {
        private const val ULID_PREFIX_BARN_UTEN_IDENT = "barnu"
    }
}
