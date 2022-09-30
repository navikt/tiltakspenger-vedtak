package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Personopplysninger
import org.intellij.lang.annotations.Language
import java.util.*

internal class PersonopplysningerDAO {

    @Language("SQL")
    private val hentPersonopplysninger = "select * from personopplysninger where søker_id = ?"

    @Language("SQL")
    private val lagrePersonopplysninger = """
        insert into personopplysninger (
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
            :skjermet,          
            :kommune,           
            :bydel,             
            :land,              
            :tidsstempelHosOss
        )""".trimIndent()

    fun lagre(søkerId: UUID, personopplysninger: Personopplysninger, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagrePersonopplysninger, mapOf(
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
                    "land" to personopplysninger.land,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    fun hent(søkerId: UUID, txSession: TransactionalSession): Personopplysninger = TODO("Not yet implemented")
}
