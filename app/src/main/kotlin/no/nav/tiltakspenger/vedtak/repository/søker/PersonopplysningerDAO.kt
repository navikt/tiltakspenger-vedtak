package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.personopplysninger.SøkerPersonopplysninger
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language

class PersonopplysningerDAO() {
    private val log = KotlinLogging.logger {}
    private val securelog = KotlinLogging.logger("tjenestekall")

    fun hent(
        søkerId: SøkerId,
        txSession: TransactionalSession,
    ): SøkerPersonopplysninger? {
        return txSession.run(
            queryOf(hentSql, søkerId.toString())
                .map { row -> row.toPersonopplysninger() }
                .asSingle,
        )
    }

    fun lagre(
        søkerId: SøkerId,
        personopplysninger: SøkerPersonopplysninger?,
        txSession: TransactionalSession,
    ) {
        log.debug { "Sletter personopplysninger før lagring" }
        slett(søkerId, txSession)

        log.debug { "Lagre personopplysninger" }

        if (personopplysninger != null) {
            lagrePersonopplysninger(søkerId, personopplysninger, txSession)
        }
    }

    private fun lagrePersonopplysninger(
        søkerId: SøkerId,
        personopplysninger: SøkerPersonopplysninger,
        txSession: TransactionalSession,
    ) {
        securelog.debug { "Lagre personopplysninger for søker $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to UlidBase.random(ULID_PREFIX_PERSONOPPLYSNINGERSØKER).toString(),
                    "sokerId" to søkerId.toString(),
                    "ident" to personopplysninger.ident,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "fortrolig" to personopplysninger.fortrolig,
                    "strengtFortrolig" to personopplysninger.strengtFortrolig,
                    "strengtFortroligUtland" to personopplysninger.strengtFortroligUtland,
                    "skjermet" to personopplysninger.skjermet,
                    "kommune" to personopplysninger.kommune,
                    "bydel" to personopplysninger.bydel,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss,
                ),
            ).asUpdate,
        )
    }

    private fun slett(søkerId: SøkerId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, søkerId.toString()).asUpdate)

    private fun Row.toPersonopplysninger(): SøkerPersonopplysninger {
        return SøkerPersonopplysninger(
            ident = string("ident"),
            fødselsdato = localDate("fødselsdato"),
            fornavn = string("fornavn"),
            mellomnavn = stringOrNull("mellomnavn"),
            etternavn = string("etternavn"),
            fortrolig = boolean("fortrolig"),
            strengtFortrolig = boolean("strengt_fortrolig"),
            strengtFortroligUtland = boolean("strengt_fortrolig_utland"),
            skjermet = booleanOrNull("skjermet"),
            kommune = stringOrNull("kommune"),
            bydel = stringOrNull("bydel"),
            tidsstempelHosOss = localDateTime("tidsstempel_hos_oss"),
        )
    }

    @Language("SQL")
    private val slettSql = "delete from personopplysninger where søker_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger where søker_id = ?"

    @Language("SQL")
    private val lagreSql = """
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
            strengt_fortrolig_utland,
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
            :strengtFortroligUtland, 
            :skjermet,          
            :kommune,           
            :bydel,             
            :tidsstempelHosOss
        )
    """.trimIndent()

    companion object {
        private const val ULID_PREFIX_PERSONOPPLYSNINGERSØKER = "person"
    }
}
