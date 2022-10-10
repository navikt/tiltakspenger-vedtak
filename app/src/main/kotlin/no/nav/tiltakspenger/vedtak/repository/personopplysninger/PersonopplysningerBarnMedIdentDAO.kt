package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Personopplysninger
import org.intellij.lang.annotations.Language
import java.util.*

internal class PersonopplysningerBarnMedIdentDAO {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(søkerId: UUID, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, søkerId).map(toPersonopplysninger).asList)

    internal fun lagre(
        søkerId: UUID,
        personopplysninger: Personopplysninger.BarnMedIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn med ident $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql, mapOf(
                    "id" to UUID.randomUUID(),
                    "sokerId" to søkerId,
                    "ident" to personopplysninger.ident,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "fortrolig" to personopplysninger.fortrolig,
                    "strengtFortrolig" to personopplysninger.strengtFortrolig,
                    "land" to personopplysninger.land,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    internal fun slett(søkerId: UUID, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, søkerId).asUpdate)

    private val toPersonopplysninger: (Row) -> Personopplysninger.BarnMedIdent = { row ->
        Personopplysninger.BarnMedIdent(
            ident = row.string("ident"),
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            fortrolig = row.boolean("fortrolig"),
            strengtFortrolig = row.boolean("strengt_fortrolig"),
            land = row.stringOrNull("land"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss")
        )
    }

    @Language("SQL")
    private val slettSql = "delete from personopplysninger_barn_med_ident where søker_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger_barn_med_ident where søker_id = ?"

    @Language("SQL")
    private val lagreSql = """
        insert into personopplysninger_barn_med_ident (
            id,
            søker_id,        
            ident,           
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            fortrolig,       
            strengt_fortrolig,
            land,           
            tidsstempel_hos_oss            
        ) values (
            :id,
            :sokerId,
            :ident,             
            :fodselsdato,   
            :fornavn,           
            :mellomnavn,        
            :etternavn,         
            :fortrolig,         
            :strengtFortrolig, 
            :land,             
            :tidsstempelHosOss
        )""".trimIndent()
}
