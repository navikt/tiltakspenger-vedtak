package no.nav.tiltakspenger.vedtak.repository.sak

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language

internal class PersonopplysningerBarnMedIdentRepo {
    private val securelog = KotlinLogging.logger("tjenestekall")

    internal fun hent(
        sakId: SakId,
        session: Session,
    ) = session.run(queryOf(hentSql, sakId.toString()).map(toPersonopplysninger).asList)

    internal fun lagre(
        sakId: SakId,
        personopplysninger: PersonopplysningerBarnMedIdent,
        session: Session,
    ) {
        securelog.info { "Lagre personopplysninger for barn med ident $personopplysninger" }
        session.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to random(ULID_PREFIX_BARN_MED_IDENT).toString(),
                    "sakId" to sakId.toString(),
                    "ident" to personopplysninger.fnr.verdi,
                    "fodselsdato" to personopplysninger.fødselsdato,
                    "fornavn" to personopplysninger.fornavn,
                    "mellomnavn" to personopplysninger.mellomnavn,
                    "etternavn" to personopplysninger.etternavn,
                    "fortrolig" to personopplysninger.fortrolig,
                    "strengtFortrolig" to personopplysninger.strengtFortrolig,
                    "strengtFortroligUtland" to personopplysninger.strengtFortroligUtland,
                    "skjermet" to personopplysninger.skjermet,
                    "oppholdsland" to personopplysninger.oppholdsland,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss,
                ),
            ).asUpdate,
        )
    }

    internal fun slett(
        sakId: SakId,
        session: Session,
    ) = session.run(queryOf(slettSql, sakId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> PersonopplysningerBarnMedIdent = { row ->
        PersonopplysningerBarnMedIdent(
            fnr = Fnr.fromString(row.string("ident")),
            fødselsdato = row.localDate("fødselsdato"),
            fornavn = row.string("fornavn"),
            mellomnavn = row.stringOrNull("mellomnavn"),
            etternavn = row.string("etternavn"),
            fortrolig = row.boolean("fortrolig"),
            strengtFortrolig = row.boolean("strengt_fortrolig"),
            strengtFortroligUtland = row.boolean("strengt_fortrolig_utland"),
            skjermet = row.booleanOrNull("skjermet"),
            oppholdsland = row.stringOrNull("oppholdsland"),
            tidsstempelHosOss = row.localDateTime("tidsstempel_hos_oss"),
        )
    }

    @Language("SQL")
    private val slettSql = "delete from sak_personopplysninger_barn_med_ident where sakId = ?"

    @Language("SQL")
    private val hentSql = "select * from sak_personopplysninger_barn_med_ident where sakId = ?"

    @Language("SQL")
    private val lagreSql =
        """
        insert into sak_personopplysninger_barn_med_ident (
            id,
            sakId,        
            ident,           
            fødselsdato,     
            fornavn,         
            mellomnavn,      
            etternavn,       
            fortrolig,       
            strengt_fortrolig,
            strengt_fortrolig_utland,
            skjermet,
            oppholdsland,           
            tidsstempel_hos_oss            
        ) values (
            :id,
            :sakId,
            :ident,             
            :fodselsdato,   
            :fornavn,           
            :mellomnavn,        
            :etternavn,         
            :fortrolig,         
            :strengtFortrolig, 
            :strengtFortroligUtland, 
            :skjermet, 
            :oppholdsland,             
            :tidsstempelHosOss
        )
        """.trimIndent()

    companion object {
        private const val ULID_PREFIX_BARN_MED_IDENT = "barnm"
    }
}
