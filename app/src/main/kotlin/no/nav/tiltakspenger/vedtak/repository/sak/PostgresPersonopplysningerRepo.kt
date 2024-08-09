package no.nav.tiltakspenger.vedtak.repository.sak

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.UlidBase
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language

internal class PostgresPersonopplysningerRepo(
    private val sessionFactory: PostgresSessionFactory,
    private val barnMedIdentDAO: PersonopplysningerBarnMedIdentRepo,
    private val barnUtenIdentDAO: PersonopplysningerBarnUtenIdentRepo,
) : PersonopplysningerRepo {
    private val log = KotlinLogging.logger {}
    private val securelog = KotlinLogging.logger("tjenestekall")

    override fun hent(sakId: SakId): SakPersonopplysninger =
        sessionFactory.withSession { session ->
            hent(sakId, session)
        }

    fun hent(
        sakId: SakId,
        session: Session,
    ): SakPersonopplysninger {
        val søker = hentPersonopplysningerForSak(sakId, session) ?: return SakPersonopplysninger()
        val barnMedIdent = barnMedIdentDAO.hent(sakId, session)
        val barnUtenIdent = barnUtenIdentDAO.hent(sakId, session)

        return SakPersonopplysninger(listOf(søker) + barnMedIdent + barnUtenIdent)
    }

    fun lagre(
        sakId: SakId,
        personopplysninger: SakPersonopplysninger,
        txSession: TransactionalSession,
    ) {
        log.info { "Sletter personopplysninger før lagring" }
        slett(sakId, txSession)
        barnMedIdentDAO.slett(sakId, txSession)
        barnUtenIdentDAO.slett(sakId, txSession)

        log.info { "Lagre personopplysninger" }
        personopplysninger.søkerOrNull()?.let { lagre(sakId, it, txSession) }
        personopplysninger.barnMedIdent().forEach { barnMedIdentDAO.lagre(sakId, it, txSession) }
        personopplysninger.barnUtenIdent().forEach { barnUtenIdentDAO.lagre(sakId, it, txSession) }
    }

    private fun hentPersonopplysningerForSak(
        sakId: SakId,
        session: Session,
    ) = session.run(queryOf(hentSql, sakId.toString()).map(toPersonopplysninger).asSingle)

    private fun lagre(
        sakId: SakId,
        personopplysninger: PersonopplysningerSøker,
        session: Session,
    ) {
        securelog.info { "Lagre personopplysninger for søker $personopplysninger" }
        session.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to UlidBase.random(ULID_PREFIX_PERSONOPPLYSNINGER).toString(),
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
                    "kommune" to personopplysninger.kommune,
                    "bydel" to personopplysninger.bydel,
                    "tidsstempelHosOss" to personopplysninger.tidsstempelHosOss,
                ),
            ).asUpdate,
        )
    }

    private fun slett(
        sakId: SakId,
        session: Session,
    ) = session.run(queryOf(slettSql, sakId.toString()).asUpdate)

    private val toPersonopplysninger: (Row) -> PersonopplysningerSøker = { row ->
        PersonopplysningerSøker(
            fnr = Fnr.fromString(row.string("ident")),
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
    private val slettSql = "delete from sak_personopplysninger_søker where sakId = ?"

    @Language("SQL")
    private val hentSql = "select * from sak_personopplysninger_søker where sakId = ?"

    @Language("SQL")
    private val lagreSql =
        """
        insert into sak_personopplysninger_søker (
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
            kommune,         
            bydel,           
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
            :kommune,           
            :bydel,             
            :tidsstempelHosOss
        )
        """.trimIndent()

    companion object {
        private const val ULID_PREFIX_PERSONOPPLYSNINGER = "poppl"
    }
}
