package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language

internal class PersonopplysningerDAO(
    private val barnMedIdentDAO: PersonopplysningerBarnMedIdentDAO = PersonopplysningerBarnMedIdentDAO(),
    private val barnUtenIdentDAO: PersonopplysningerBarnUtenIdentDAO = PersonopplysningerBarnUtenIdentDAO(),
) {
    private val log = KotlinLogging.logger {}
    private val securelog = KotlinLogging.logger("tjenestekall")

    fun hent(
        innsendingId: InnsendingId,
        txSession: TransactionalSession,
    ): List<Personopplysninger> {
        val søker = hentPersonopplysningerForInnsending(innsendingId, txSession) ?: return emptyList()
        val barnMedIdent = barnMedIdentDAO.hent(innsendingId, txSession)
        val barnUtenIdent = barnUtenIdentDAO.hent(innsendingId, txSession)

        return listOf(søker) + barnMedIdent + barnUtenIdent
    }

    fun lagre(
        innsendingId: InnsendingId,
        personopplysninger: List<Personopplysninger>,
        txSession: TransactionalSession,
    ) {
        log.info { "Sletter personopplysninger før lagring" }
        slett(innsendingId, txSession)
        barnMedIdentDAO.slett(innsendingId, txSession)
        barnUtenIdentDAO.slett(innsendingId, txSession)

        log.info { "Lagre personopplysninger" }
        personopplysninger.forEach {
            when (it) {
                is PersonopplysningerSøker -> lagre(innsendingId, it, txSession)
                is PersonopplysningerBarnMedIdent -> barnMedIdentDAO.lagre(innsendingId, it, txSession)
                is PersonopplysningerBarnUtenIdent -> barnUtenIdentDAO.lagre(
                    innsendingId,
                    it,
                    txSession,
                )
            }
        }
    }

    private fun hentPersonopplysningerForInnsending(
        innsendingId: InnsendingId,
        txSession: TransactionalSession,
    ): PersonopplysningerSøker? =
        txSession.run(queryOf(hentSql, innsendingId.toString()).map(toPersonopplysninger).asSingle)

    private fun lagre(
        innsendingId: InnsendingId,
        personopplysninger: PersonopplysningerSøker,
        txSession: TransactionalSession,
    ) {
        securelog.info { "Lagre personopplysninger for søker $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to UlidBase.random(ULID_PREFIX_PERSONOPPLYSNINGER).toString(),
                    "innsendingId" to innsendingId.toString(),
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

    private fun slett(innsendingId: InnsendingId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, innsendingId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> PersonopplysningerSøker = { row ->
        PersonopplysningerSøker(
            ident = row.string("ident"),
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            fortrolig = row.boolean("fortrolig"),
            strengtFortrolig = row.boolean("strengt_fortrolig"),
            strengtFortroligUtland = row.boolean("strengt_fortrolig_utland"),
            skjermet = row.booleanOrNull("skjermet"),
            kommune = row.stringOrNull("kommune"),
            bydel = row.stringOrNull("bydel"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss"),
        )
    }

    @Language("SQL")
    private val slettSql = "delete from personopplysninger_søker where innsending_id = ?"

    @Language("SQL")
    private val hentSql = "select * from personopplysninger_søker where innsending_id = ?"

    @Language("SQL")
    private val lagreSql = """
        insert into personopplysninger_søker (
            id,
            innsending_id,        
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
            :innsendingId,
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
        private const val ULID_PREFIX_PERSONOPPLYSNINGER = "poppl"
    }
}
