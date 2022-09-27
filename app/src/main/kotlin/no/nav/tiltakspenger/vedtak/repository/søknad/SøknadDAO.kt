package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language

internal class SøknadDAO(
    private val barnetilleggDAO: BarnetilleggDAO = BarnetilleggDAO(),
    private val tiltakDAO: TiltakDAO = TiltakDAO(),
    private val trygdOgPensjonDAO: TrygdOgPensjonDAO = TrygdOgPensjonDAO(),
) {
    fun hentAlle(søkerId: UUID, txSession: TransactionalSession): List<Søknad> {
        return txSession.run(
            queryOf(hentAlle, søkerId)
                .map { row ->
                    row.toSøknad(txSession)
                }.asList
        )
    }

    fun lagre(søkerId: UUID, søknader: List<Søknad>, txSession: TransactionalSession) {
        søknader.forEach {
            lagreHeleSøknaden(søkerId, it, txSession)
        }
    }

    private fun søknadFinnes(søknadId: UUID, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, søknadId).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if person exists")

    private fun lagreHeleSøknaden(søkerId: UUID, søknad: Søknad, txSession: TransactionalSession) {
        if (søknadFinnes(søknad.id, txSession)) {
            oppdaterSøknad(søknad, txSession)
        } else {
            lagreSøknad(søkerId, søknad, txSession)
        }
        barnetilleggDAO.lagre(søknad.id, søknad.barnetillegg, txSession)
        tiltakDAO.lagre(søknad.id, søknad.tiltak, txSession)
        trygdOgPensjonDAO.lagre(søknad.id, søknad.trygdOgPensjon, txSession)
    }

    private fun oppdaterSøknad(søknad: Søknad, txSession: TransactionalSession) {
        txSession.run(
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
                    "opprettet" to søknad.opprettet,
                    "tidsstempelHosOss" to søknad.tidsstempelHosOss,
                )
            ).asUpdate
        )
    }

    private fun lagreSøknad(søkerId: UUID, søknad: Søknad, txSession: TransactionalSession) {
        txSession.run(
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
                    "opprettet" to søknad.opprettet,
                    "tidsstempelHosOss" to søknad.tidsstempelHosOss,
                )
            ).asUpdate
        )
    }

    private fun Row.toSøknad(txSession: TransactionalSession): Søknad {
        val id = uuid("id")
        val søknadId = string("søknad_id")
        val fornavn = stringOrNull("fornavn")
        val etternavn = stringOrNull("etternavn")
        val ident = string("ident")
        val deltarKvp = boolean("deltar_kvp")
        val deltarIntroduksjonsprogrammet = booleanOrNull("deltar_intro")
        val oppholdInstitusjon = booleanOrNull("institusjon_opphold")
        val typeInstitusjon = stringOrNull("institusjon_type")
        val opprettet = localDateTimeOrNull("opprettet")
        val tidsstempelHosOss = localDateTime("tidsstempel_hos_oss")
        val dokumentInfoId = string("dokumentinfo_id")
        val journalpostId = string("journalpost_id")
        val fritekst = stringOrNull("fritekst")
        val barnetillegg = barnetilleggDAO.hentBarnetilleggListe(id, txSession)
        val tiltak = tiltakDAO.hent(id, txSession)
        val trygdOgPensjon = trygdOgPensjonDAO.hentTrygdOgPensjonListe(id, txSession)

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
            tidsstempelHosOss = tidsstempelHosOss,
            tiltak = tiltak,
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
            opprettet,
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
            :opprettet,
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
            opprettet = :opprettet,
            tidsstempel_hos_oss = :tidsstempelHosOss
        where id = :id
        """.trimIndent()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søknad where id = ?)"

    @Language("SQL")
    private val hentAlle = "select * from søknad where søker_id = ?"
}
