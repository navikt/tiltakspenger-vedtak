package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language
import java.util.*

internal class PersonopplysningerDAO {
    private val log = KotlinLogging.logger {}
    private val securelog = KotlinLogging.logger("tjenestekall")
    private val toPersonopplysningerSøker: (Row) -> Personopplysninger.Søker = { row ->
        Personopplysninger.Søker(
            row.string("ident"),
            row.localDate("fødselsdato"),
            row.string("fornavn"),
            row.stringOrNull("mellomnavn"),
            row.string("etternavn"),
            row.boolean("fortrolig"),
            row.boolean("strengt_fortrolig"),
            row.booleanOrNull("skjermet"),
            row.stringOrNull("kommune"),
            row.stringOrNull("bydel"),
            row.localDateTime("tidsstempel_hos_oss")
        )
    }

    @Language("SQL")
    private val slettPersonopplysningerSøker = "delete from personopplysninger_søker where søker_id = ?"

    @Language("SQL")
    private val slettPersonopplysningerBarnMedIdent = "delete from personopplysninger_barn_med_ident where søker_id = ?"

    @Language("SQL")
    private val slettPersonopplysningerBarnUtenIdent =
        "delete from personopplysninger_barn_uten_ident where søker_id = ?"

    @Language("SQL")
    private val hentPersonopplysningerSøker =
        "select * from personopplysninger_søker where søker_id = :sokerId"

    @Language("SQL")
    private val lagrePersonopplysningerSøker = """
        insert into personopplysninger_søker (
            id,
            søker_id,        
            ident,           
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            fortrolig,       
            strengt_fortrolig,
            skjermet,        
            kommune,         
            bydel,           
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
            :skjermet,          
            :kommune,           
            :bydel,             
            :tidsstempelHosOss
        )""".trimIndent()

    @Language("SQL")
    private val lagrePersonopplysningerBarnMedIdent = """
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

    @Language("SQL")
    private val lagrePersonopplysningerBarnUtenIdent = """
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


    private fun lagreSøker(
        søkerId: UUID,
        personopplysninger: Personopplysninger.Søker,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger $søkerId" }
        txSession.run(
            queryOf(
                lagrePersonopplysningerSøker, mapOf(
                    "id" to UUID.randomUUID(),
                    "sokerId" to søkerId,
                    "ident" to personopplysninger.ident,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "fortrolig" to personopplysninger.fortrolig,
                    "strengtFortrolig" to personopplysninger.strengtFortrolig,
                    "skjermet" to personopplysninger.skjermet,
                    "kommune" to personopplysninger.kommune,
                    "bydel" to personopplysninger.bydel,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    private fun lagreBarnMedIdent(
        søkerId: UUID,
        personopplysninger: Personopplysninger.BarnMedIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn med ident $søkerId" }
        txSession.run(
            queryOf(
                lagrePersonopplysningerBarnMedIdent, mapOf(
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

    private fun lagreBarnUtenIdent(
        søkerId: UUID,
        personopplysninger: Personopplysninger.BarnUtenIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn uten ident $søkerId" }
        txSession.run(
            queryOf(
                lagrePersonopplysningerBarnUtenIdent, mapOf(
                    "id" to UUID.randomUUID(),
                    "sokerId" to søkerId,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    fun lagre(søkerId: UUID, personopplysninger: List<Personopplysninger>, txSession: TransactionalSession) {
        log.info { "Sletter personopplysninger før lagring" }
        txSession.run(queryOf(slettPersonopplysningerSøker, søkerId).asUpdate)
        txSession.run(queryOf(slettPersonopplysningerBarnMedIdent, søkerId).asUpdate)
        txSession.run(queryOf(slettPersonopplysningerBarnUtenIdent, søkerId).asUpdate)

        personopplysninger.forEach {
            when (it) {
                is Personopplysninger.Søker -> lagreSøker(søkerId, it, txSession)
                is Personopplysninger.BarnMedIdent -> lagreBarnMedIdent(søkerId, it, txSession)
                is Personopplysninger.BarnUtenIdent -> lagreBarnUtenIdent(søkerId, it, txSession)
            }
        }

        log.info { "Lagre personopplysninger" }
    }

    fun hentPersonopplysningerForBarnMedIdent(søkerId: UUID, txSession: TransactionalSession) =
        txSession.run(
            queryOf(
                hentPersonopplysningerSøker,
                mapOf("sokerId" to søkerId, "erBarn" to true)
            ).map(toPersonopplysningerSøker).asList
        )

    fun hentPersonopplysningerForSøker(
        søkerId: UUID,
        txSession: TransactionalSession
    ): Personopplysninger.Søker? = txSession.run(
        queryOf(
            hentPersonopplysningerSøker,
            mapOf("sokerId" to søkerId, "erBarn" to false)
        ).map(toPersonopplysningerSøker).asSingle
    )
}
