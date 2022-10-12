package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.vedtak.Personopplysninger
import org.intellij.lang.annotations.Language


internal class PersonopplysningerBarnUtenIdentDAO {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(søkerId: SøkerId, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, søkerId.toString()).map(toPersonopplysninger).asList)

    internal fun lagre(
        søkerId: SøkerId,
        personopplysninger: Personopplysninger.BarnUtenIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn uten ident $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql, mapOf(
                    "id" to UlidBase.new(ULID_PREFIX_BARN_UTEN_IDENT).toString(),
                    "sokerId" to søkerId.toString(),
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    internal fun slett(søkerId: SøkerId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, søkerId.toString()).asUpdate)

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
    private val slettSql = "delete from personopplysninger_barn_uten_ident where søker_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger_barn_uten_ident where søker_id = ?"

    @Language("SQL")
    private val lagreSql = """
        insert into personopplysninger_barn_uten_ident (
            id,
            søker_id,        
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            tidsstempel_hos_oss            
        ) values (
            :id,
            :sokerId,
            :fodselsdato,   
            :fornavn,           
            :mellomnavn,        
            :etternavn,         
            :tidsstempelHosOss
        )""".trimIndent()

    companion object {
        private const val ULID_PREFIX_BARN_UTEN_IDENT = "barnu"
    }
}
