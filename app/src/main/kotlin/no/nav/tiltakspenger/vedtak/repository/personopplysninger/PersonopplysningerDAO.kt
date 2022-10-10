package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language
import java.util.*

internal class PersonopplysningerDAO(
    private val barnMedIdentDAO: PersonopplysningerBarnMedIdentDAO = PersonopplysningerBarnMedIdentDAO(),
    private val barnUtenIdentDAO: PersonopplysningerBarnUtenIdentDAO = PersonopplysningerBarnUtenIdentDAO()
) {
    private val log = KotlinLogging.logger {}
    private val securelog = KotlinLogging.logger("tjenestekall")

    fun hent(
        søkerId: UUID,
        txSession: TransactionalSession,
    ): List<Personopplysninger> {
        val søker = hentPersonopplysningerForSøker(søkerId, txSession) ?: return emptyList()
        val barnMedIdent = barnMedIdentDAO.hent(søkerId, txSession)
        val barnUtenIdent = barnUtenIdentDAO.hent(søkerId, txSession)

        return listOf(søker) + barnMedIdent + barnUtenIdent
    }

    fun lagre(søkerId: UUID, personopplysninger: List<Personopplysninger>, txSession: TransactionalSession) {
        log.info { "Sletter personopplysninger før lagring" }
        slett(søkerId, txSession)
        barnMedIdentDAO.slett(søkerId, txSession)
        barnUtenIdentDAO.slett(søkerId, txSession)

        personopplysninger.forEach {
            when (it) {
                is Personopplysninger.Søker -> lagre(søkerId, it, txSession)
                is Personopplysninger.BarnMedIdent -> barnMedIdentDAO.lagre(søkerId, it, txSession)
                is Personopplysninger.BarnUtenIdent -> barnUtenIdentDAO.lagre(
                    søkerId,
                    it,
                    txSession
                )
            }
        }
        log.info { "Lagre personopplysninger" }
    }

    private fun hentPersonopplysningerForSøker(søkerId: UUID, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, søkerId).map(toPersonopplysninger).asSingle)

    private fun lagre(
        søkerId: UUID,
        personopplysninger: Personopplysninger.Søker,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for søker $personopplysninger" }
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
                    "skjermet" to personopplysninger.skjermet,
                    "kommune" to personopplysninger.kommune,
                    "bydel" to personopplysninger.bydel,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    private fun slett(søkerId: UUID, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, søkerId).asUpdate)

    private val toPersonopplysninger: (Row) -> Personopplysninger.Søker = { row ->
        Personopplysninger.Søker(
            ident = row.string("ident"),
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            fortrolig = row.boolean("fortrolig"),
            strengtFortrolig = row.boolean("strengt_fortrolig"),
            skjermet = row.booleanOrNull("skjermet"),
            kommune = row.stringOrNull("kommune"),
            bydel = row.stringOrNull("bydel"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss")
        )
    }

    @Language("SQL")
    private val slettSql = "delete from personopplysninger_søker where søker_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger_søker where søker_id = ?"

    @Language("SQL")
    private val lagreSql = """
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
}
