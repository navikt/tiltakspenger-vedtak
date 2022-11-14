package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Personopplysninger
import org.intellij.lang.annotations.Language


internal class PersonopplysningerBarnMedIdentDAO {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(innsendingId: InnsendingId, txSession: TransactionalSession) =
        txSession.run(queryOf(hentSql, innsendingId.toString()).map(toPersonopplysninger).asList)

    internal fun lagre(
        innsendingId: InnsendingId,
        personopplysninger: Personopplysninger.BarnMedIdent,
        txSession: TransactionalSession
    ) {
        securelog.info { "Lagre personopplysninger for barn med ident $personopplysninger" }
        txSession.run(
            queryOf(
                lagreSql, mapOf(
                    "id" to random(ULID_PREFIX_BARN_MED_IDENT).toString(),
                    "sokerId" to innsendingId.toString(),
                    "ident" to personopplysninger.ident,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "fortrolig" to personopplysninger.fortrolig,
                    "strengtFortrolig" to personopplysninger.strengtFortrolig,
                    "strengtFortroligUtland" to personopplysninger.strengtFortroligUtland,
                    "oppholdsland" to personopplysninger.oppholdsland,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    internal fun slett(innsendingId: InnsendingId, txSession: TransactionalSession) =
        txSession.run(queryOf(slettSql, innsendingId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> Personopplysninger.BarnMedIdent = { row ->
        Personopplysninger.BarnMedIdent(
            ident = row.string("ident"),
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            fortrolig = row.boolean("fortrolig"),
            strengtFortrolig = row.boolean("strengt_fortrolig"),
            strengtFortroligUtland = row.boolean("strengt_fortrolig_utland"),
            oppholdsland = row.stringOrNull("oppholdsland"),
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
            strengt_fortrolig_utland,
            oppholdsland,           
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
            :oppholdsland,             
            :tidsstempelHosOss
        )""".trimIndent()

    companion object {
        private const val ULID_PREFIX_BARN_MED_IDENT = "barnm"
    }
}
