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
    private val toPersonopplysninger: (Row) -> Personopplysninger = { row ->
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

    private fun personopplysningerFinnes(ident: String, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, ident).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw Exception("Failed to check if personopplysninger exists")

    @Language("SQL")
    private val finnes = "select exists(select 1 from personopplysninger where ident = ?)"

    @Language("SQL")
    private val slettPersonopplysninger = "delete from personopplysninger where søker_id = ?"

    @Language("SQL")
    private val hentPersonopplysninger =
        "select * from personopplysninger where søker_id = :sokerId AND er_barn = :erBarn"

    @Language("SQL")
    private val lagrePersonopplysninger = """
        insert into personopplysninger (
            id,
            søker_id,        
            ident,           
            fødselsdato,     
            er_barn,
            fornavn,         
            mellomnavn,      
            etternavn,       
            fortrolig,       
            strengt_fortrolig,
            skjermet,        
            kommune,         
            bydel,           
            land,
            tidsstempel_hos_oss            
        ) values (
            :id,
            :sokerId,
            :ident,             
            :fodselsdato,   
            :erBarn,
            :fornavn,           
            :mellomnavn,        
            :etternavn,         
            :fortrolig,         
            :strengtFortrolig, 
            :skjermet,          
            :kommune,           
            :bydel,             
            :land,              
            :tidsstempelHosOss
        )""".trimIndent()

    fun lagre(søkerId: UUID, personopplysninger: List<Personopplysninger>, txSession: TransactionalSession) {
        log.info { "Sletter personopplysninger før lagring" }
        txSession.run(queryOf(slettPersonopplysninger, søkerId).asUpdate)
        log.info { "Lagre personopplysninger" }
        securelog.info { "Lagre personopplysninger $søkerId" }
        personopplysninger.forEach {
            txSession.run(
                queryOf(
                    lagrePersonopplysninger, mapOf(
                        "id" to UUID.randomUUID(),
                        "sokerId" to søkerId,
                        "ident" to it.ident,
                        "fodselsdato" to it.fødselsdato,
                        "erBarn" to it.erBarn,
                        "fornavn" to it.fornavn,
                        "mellomnavn" to it.mellomnavn,
                        "etternavn" to it.etternavn,
                        "fortrolig" to it.fortrolig,
                        "strengtFortrolig" to it.strengtFortrolig,
                        "skjermet" to it.skjermet,
                        "kommune" to it.kommune,
                        "bydel" to it.bydel,
                        "land" to it.land,
                        "tidsstempelHosOss" to it.tidsstempelHosOss
                    )
                ).asUpdate
            )
        }
    }

    fun hentPersonopplysningerForBarn(søkerId: UUID, txSession: TransactionalSession): List<Personopplysninger> =
        txSession.run(
            queryOf(
                hentPersonopplysninger,
                mapOf("sokerId" to søkerId, "erBarn" to true)
            ).map(toPersonopplysninger).asList
        )

    fun hentPersonopplysningerForSøker(
        søkerId: UUID,
        txSession: TransactionalSession
    ): Personopplysninger? = txSession.run(
        queryOf(
            hentPersonopplysninger,
            mapOf("sokerId" to søkerId, "erBarn" to false)
        ).map(toPersonopplysninger).asSingle
    )
}
