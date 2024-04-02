package no.nav.tiltakspenger.vedtak.repository.sak

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import org.intellij.lang.annotations.Language

internal class PersonopplysningerBarnUtenIdentRepo {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(sakId: SakId, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, sakId.toString()).map(toPersonopplysninger).asList)

    internal fun lagre(
        sakId: SakId,
        personopplysninger: PersonopplysningerBarnUtenIdent,
        txSession: TransactionalSession,
    ) {
        securelog.info { "Lagre personopplysninger for barn uten ident $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to random(ULID_PREFIX_BARN_UTEN_IDENT).toString(),
                    "sakId" to sakId.toString(),
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss,
                ),
            ).asUpdate,
        )
    }

    internal fun slett(sakId: SakId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, sakId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> PersonopplysningerBarnUtenIdent = { row ->
        PersonopplysningerBarnUtenIdent(
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss"),
        )
    }

    @Language("SQL")
    private val slettSql = "delete from sak_personopplysninger_barn_uten_ident where sakId = ?"

    @Language("SQL")
    private val hentSql = "select * from sak_personopplysninger_barn_uten_ident where sakId = ?"

    @Language("SQL")
    private val lagreSql = """
        insert into sak_personopplysninger_barn_uten_ident (
            id,
            sakId,        
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            tidsstempel_hos_oss            
        ) values (
            :id,
            :sakId,
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
