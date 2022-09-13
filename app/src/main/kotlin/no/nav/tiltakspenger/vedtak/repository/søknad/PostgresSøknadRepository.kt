package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import org.intellij.lang.annotations.Language

internal class PostgresSøknadRepository : SøknadRepository {

    private val barnetilleggRepo = BarnetilleggRepo()

    @Language("SQL")
    private val lagreSøknad = """
        insert into søknad (
            id, 
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
    private val lagreArenaTiltak = """
        insert into arenatiltak (
            id,
            søknad_id,
            arena_id,
            arrangoer, 
            har_sluttdato_fra_arena, 
            navn,
            er_i_endre_status,
            opprinnelig_startdato,
            opprinnelig_sluttdato,
            startdato,
            sluttdato
        ) values (
            :id,
            :soknadId,
            :arenaId,
            :arrangoer, 
            :harSluttdatoFraArena,
            :navn,
            :erIEndreStatus,
            :opprinneligStartdato,
            :opprinneligSluttdato,
            :startdato,
            :sluttdato
        )""".trimIndent()

    @Language("SQL")
    private val lagreBrukerTiltak = """
        insert into brukertiltak (
            id,
            søknad_id,
            tiltakstype,
            arrangoernavn, 
            beskrivelse, 
            fom,
            tom,
            adresse,
            postnummer,
            antall_dager
        ) values (
            :id,
            :soknadId,
            :tiltakstype,
            :arrangoernavn, 
            :beskrivelse,
            :fom,
            :tom,
            :adresse,
            :postnummer,
            :antallDager
        )""".trimIndent()

    @Language("SQL")
    private val lagreTrygdOgPensjon = """
        insert into trygdogpensjon (
            id,
            søknad_id,
            utbetaler,
            prosent,
            fom,
            tom
        ) values (
            :id,
            :soknadId,
            :utbetaler,
            :prosent,
            :fom,
            :tom
        )""".trimIndent()


    @Language("SQL")
    private val slettArenaTiltak = "delete from arenatiltak where søknad_id = ?"

    @Language("SQL")
    private val slettBrukerregistrertTiltak = "delete from brukertiltak where søknad_id = ?"

    @Language("SQL")
    private val slettTrygdOgPensjon = "delete from trygdogpensjon where søknad_id = ?"

    @Language("SQL")
    private val finnes = "select exists(select 1 from søknad where id = ?)"

    // TODO: Denne liker jeg ikke, bør vi ikke spørre på UUIDen (fremmednøkkelen)??
    @Language("SQL")
    private val hentAlle = "select * from søknad where ident = ?"

    override fun hentAlle(ident: String): List<Søknad> {
        return session.run(
            queryOf(hentAlle, ident)
                .map { row ->
                    row.toSøknad()
                }.asList
        )
    }

    private fun slettArenatiltak(søknadId: UUID): Unit {
        session.run(
            queryOf(slettArenaTiltak, søknadId).asUpdate
        )
    }

    private fun slettBrukertiltak(søknadId: UUID): Unit {
        session.run(
            queryOf(slettBrukerregistrertTiltak, søknadId).asUpdate
        )
    }

    private fun slettTrygdOgPensjon(søknadId: UUID): Unit {
        session.run(
            queryOf(slettTrygdOgPensjon, søknadId).asUpdate
        )
    }

    private fun søknadFinnes(søknadId: UUID): Boolean = session.run(
        queryOf(finnes, søknadId).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    private fun lagreHeleSøknaden(søknad: Søknad) {
        barnetilleggRepo.lagre(søknad.id, søknad.barnetillegg)

        slettArenatiltak(søknad.id)
        if (søknad.arenaTiltak != null) lagreArenaTiltak(søknad.id, søknad.arenaTiltak!!)
        slettBrukertiltak(søknad.id)
        if (søknad.brukerregistrertTiltak != null) lagreBrukerTiltak(søknad.id, søknad.brukerregistrertTiltak!!)
        slettTrygdOgPensjon(søknad.id)
        søknad.trygdOgPensjon?.forEach { lagreTrygdOgPensjon(søknad.id, it) }

        if (søknadFinnes(søknad.id)) {
            oppdaterSøknad(søknad)
        } else {
            lagreSøknad(søknad)
        }
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
        session.run(
            queryOf(
                lagreSøknad, mapOf(
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


    private fun lagreArenaTiltak(søknadId: UUID, arenaTiltak: ArenaTiltak) {
        session.run(
            queryOf(
                lagreArenaTiltak, mapOf(
                    "id" to UUID.randomUUID(),
                    "soknadId" to søknadId,
                    "arenaId" to arenaTiltak.arenaId,
                    "arrangoer" to arenaTiltak.arrangoer,
                    "harSluttdatoFraArena" to arenaTiltak.harSluttdatoFraArena,
                    "navn" to arenaTiltak.tiltakskode,                  // TODO sjekk om denne er riktig, og om den skal endre navn i basen
                    "erIEndreStatus" to arenaTiltak.erIEndreStatus,
                    "opprinneligStartdato" to arenaTiltak.opprinneligStartdato,
                    "opprinneligSluttdato" to arenaTiltak.opprinneligSluttdato,
                    "startdato" to arenaTiltak.startdato,
                    "sluttdato" to arenaTiltak.sluttdato,
                )
            ).asUpdate
        )
    }

    private fun lagreBrukerTiltak(søknadId: UUID, brukerregistrertTiltak: BrukerregistrertTiltak) {
        session.run(
            queryOf(
                lagreBrukerTiltak, mapOf(
                    "id" to UUID.randomUUID(),
                    "soknadId" to søknadId,
                    "tiltakstype" to brukerregistrertTiltak.tiltakskode,    // TODO skal denne endre navn i basen ?
                    "arrangoernavn" to brukerregistrertTiltak.arrangoernavn,
                    "beskrivelse" to brukerregistrertTiltak.beskrivelse,
                    "fom" to brukerregistrertTiltak.fom,
                    "tom" to brukerregistrertTiltak.tom,
                    "adresse" to brukerregistrertTiltak.adresse,
                    "postnummer" to brukerregistrertTiltak.postnummer,
                    "antallDager" to brukerregistrertTiltak.antallDager,
                )
            ).asUpdate
        )
    }

    private fun lagreTrygdOgPensjon(søknadId: UUID, trygdOgPensjon: TrygdOgPensjon) {
        session.run(
            queryOf(
                lagreTrygdOgPensjon, mapOf(
                    "id" to UUID.randomUUID(),
                    "soknadId" to søknadId,
                    "utbetaler" to trygdOgPensjon.utbetaler,
                    "prosent" to trygdOgPensjon.prosent,
                    "fom" to trygdOgPensjon.fom,
                    "tom" to trygdOgPensjon.tom,
                )
            ).asUpdate
        )
    }

    override fun lagre(ident: String, søknader: List<Søknad>): Int {
        søknader.forEach {
            lagreHeleSøknaden(it)
        }
        return 0
    }

    private fun Row.toSøknad(): Søknad {
        val id = uuid("id")
        val fornavn = string("fornavn")
        val etternavn = string("etternavn")
        val ident = string("ident")
        val deltarKvp = boolean("deltar_kvp")
        val deltarIntroduksjonsprogrammet = boolean("deltar_introduksjon")
        val oppholdInstitusjon = boolean("institusjon_opphold")
        val typeInstitusjon = string("institusjon_type")
        val tiltaksArrangør = string("tiltaks_arrangoer")
        val tiltaksType = string("tiltaks_type")
        val opprettet = localDateTime("opprettet")
        val brukerRegistrertStartDato = localDate("bruker_reg_startdato")
        val brukerRegistrertSluttDato = localDate("bruker_reg_sluttdato")
        val systemRegistrertStartDato = localDate("system_reg_startdato")
        val systemRegistrertSluttDato = localDate("system_reg_sluttdato")
        val innhentet = localDateTime("innhentet")
        val barnetillegg = barnetilleggRepo.hentBarnetilleggListe(id)

        return Søknad(
            id = id,
            søknadId = "",
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
            arenaTiltak = null,
            brukerregistrertTiltak = null,
            trygdOgPensjon = null,
            fritekst = null,
            dokumentInfoId = "",
            journalpostId = "",
        )
    }
}
