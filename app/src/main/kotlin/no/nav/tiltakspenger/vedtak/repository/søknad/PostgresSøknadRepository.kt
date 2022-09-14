package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language
import java.util.*

internal class PostgresSøknadRepository : SøknadRepository {

    private val barnetilleggRepo = BarnetilleggRepo()
    private val arenatiltakRepo = ArenatiltakRepo()
    private val brukertiltakRepo = BrukertiltakRepo()
    private val trygdOgPensjonRepo = TrygdOgPensjonRepo()

    override fun hentAlle(ident: String): List<Søknad> {
        return session.run(
            queryOf(hentAlle, ident)
                .map { row ->
                    row.toSøknad()
                }.asList
        )
    }

    override fun lagre(ident: String, søknader: List<Søknad>): Int {
        søknader.forEach {
            lagreHeleSøknaden(it)
        }
        return søknader.size
    }

    private fun søknadFinnes(søknadId: UUID): Boolean = session.run(
        queryOf(finnes, søknadId).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    private fun hentSøkerId(ident: String): UUID = session.run(
        queryOf(hentSøkerId, ident).map { row -> row.uuid("id") }.asSingle
    ) ?: throw InternalError("Finnes ingen søker for ident $ident")

    private fun lagreHeleSøknaden(søknad: Søknad) {
        if (søknadFinnes(søknad.id)) {
            oppdaterSøknad(søknad)
        } else {
            lagreSøknad(søknad)
        }
        barnetilleggRepo.lagre(søknad.id, søknad.barnetillegg)
        arenatiltakRepo.lagre(søknad.id, søknad.arenaTiltak)
        brukertiltakRepo.lagre(søknad.id, søknad.brukerregistrertTiltak)
        trygdOgPensjonRepo.lagre(søknad.id, søknad.trygdOgPensjon)
    }

    private fun oppdaterSøknad(søknad: Søknad) {
        session.run(
            queryOf(
                oppdaterSøknad, mapOf(
                    "id" to søknad.id,
                    "fornavn" to søknad.fornavn,
                    "etternavn" to søknad.etternavn,
                    "ident" to søknad.ident,
                    "deltarKvp" to søknad.deltarKvp,
                    "deltarIntro" to søknad.deltarIntroduksjonsprogrammet,
                    "instOpphold" to søknad.oppholdInstitusjon,
                    "instType" to søknad.typeInstitusjon,
                    "fritekst" to søknad.fritekst,
                    "journalpostId" to søknad.journalpostId,
                    "dokumentinfoId" to søknad.dokumentInfoId,
                    "tidsstempelKilde" to søknad.opprettet,
                    "tidsstempelHosOss" to søknad.innhentet,
                )
            ).asUpdate
        )
    }

    private fun lagreSøknad(søknad: Søknad) {
        val søkerId = hentSøkerId(søknad.ident)
        session.run(
            queryOf(
                lagreSøknad, mapOf(
                    "id" to søknad.id,
                    "sokerId" to søkerId,
                    "eksternSoknadId" to søknad.søknadId,
                    "fornavn" to søknad.fornavn,
                    "etternavn" to søknad.etternavn,
                    "ident" to søknad.ident,
                    "deltarKvp" to søknad.deltarKvp,
                    "deltarIntro" to søknad.deltarIntroduksjonsprogrammet,
                    "instOpphold" to søknad.oppholdInstitusjon,
                    "instType" to søknad.typeInstitusjon,
                    "fritekst" to søknad.fritekst,
                    "journalpostId" to søknad.journalpostId,
                    "dokumentinfoId" to søknad.dokumentInfoId,
                    "tidsstempelKilde" to søknad.oppdatert(),
                    "tidsstempelHosOss" to søknad.innhentet,
                )
            ).asUpdate
        )
    }

    private fun Row.toSøknad(): Søknad {
        val id = uuid("id")
        val søknadId = string("søknad_id")
        val fornavn = stringOrNull("fornavn")
        val etternavn = stringOrNull("etternavn")
        val ident = string("ident")
        val deltarKvp = boolean("deltar_kvp")
        val deltarIntroduksjonsprogrammet = booleanOrNull("deltar_intro")
        val oppholdInstitusjon = booleanOrNull("institusjon_opphold")
        val typeInstitusjon = stringOrNull("institusjon_type")
        val opprettet = localDateTime("tidsstempel_hos_oss")
        val innhentet = localDateTime("tidsstempel_kilde")
        val dokumentInfoId = string("dokumentinfo_id")
        val journalpostId = string("journalpost_id")
        val fritekst = stringOrNull("fritekst")
        val barnetillegg = barnetilleggRepo.hentBarnetilleggListe(id)
        val arenaTiltak = arenatiltakRepo.hent(id)
        val brukerTiltak = brukertiltakRepo.hent(id)
        val trygdOgPensjon = trygdOgPensjonRepo.hentTrygdOgPensjonListe(id)

        return Søknad(
            id = id,
            søknadId = søknadId,
            fornavn = fornavn,
            etternavn = etternavn,
            ident = ident,
            deltarKvp = deltarKvp,
            deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
            oppholdInstitusjon = oppholdInstitusjon,
            typeInstitusjon = typeInstitusjon,
            opprettet = opprettet,
            barnetillegg = barnetillegg,
            innhentet = innhentet,
            arenaTiltak = arenaTiltak,
            brukerregistrertTiltak = brukerTiltak,
            trygdOgPensjon = trygdOgPensjon,
            fritekst = fritekst,
            dokumentInfoId = dokumentInfoId,
            journalpostId = journalpostId,
        )
    }

    @Language("SQL")
    private val lagreSøknad = """
        insert into søknad (
            id,
            søker_id,
            søknad_id,
            fornavn, 
            etternavn, 
            ident, 
            deltar_kvp, 
            deltar_intro, 
            institusjon_opphold, 
            institusjon_type,
            fritekst,
            journalpost_id,
            dokumentinfo_id,
            tidsstempel_kilde,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :sokerId,
            :eksternSoknadId,
            :fornavn, 
            :etternavn,
            :ident,
            :deltarKvp,
            :deltarIntro,
            :instOpphold,
            :instType,
            :fritekst,
            :journalpostId,
            :dokumentinfoId,
            :tidsstempelKilde,
            :tidsstempelHosOss
        )""".trimIndent()

    @Language("SQL")
    private val oppdaterSøknad = """
        update søknad set  
            fornavn = :fornavn, 
            etternavn = :etternavn, 
            ident = :ident, 
            deltar_kvp = :deltarKvp, 
            deltar_intro = :deltarIntro, 
            institusjon_opphold = :instOpphold, 
            institusjon_type = :instType,
            fritekst = :fritekst,
            journalpost_id = :journalpostId,
            dokumentinfo_id = :dokumentinfoId,
            tidsstempel_kilde = :tidsstempelKilde,
            tidsstempel_hos_oss = :tidsstempelHosOss
        where id = :id
        )""".trimIndent()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søknad where id = ?)"

    // TODO Hågen liker ikke denne. Lurer på om vi kan bruke id istedet for ident (rotet bort kommentaren så måtte skrive en ny :-) )
    @Language("SQL")
    private val hentAlle = "select * from søknad where ident = ?"

    @Language("SQL")
    private val hentSøkerId = "select id from søker where ident = ?"
}
