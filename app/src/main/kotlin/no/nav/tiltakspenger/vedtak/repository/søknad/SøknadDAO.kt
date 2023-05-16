package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Søknad.PeriodeSpm
import no.nav.tiltakspenger.vedtak.db.booleanOrNull
import org.intellij.lang.annotations.Language
import sun.jvm.hotspot.oops.CellTypeState.value

private const val JA = "Ja"
private const val NEI = "Nei"
private const val IKKE_RELEVANT = "IkkeRelevant"
private const val IKKE_MED_I_SØKNADEN = "IkkeMedISøknaden"

internal class SøknadDAO(
    private val barnetilleggDAO: BarnetilleggDAO = BarnetilleggDAO(),
    private val tiltakDAO: TiltakDAO = TiltakDAO(),
    private val vedleggDAO: VedleggDAO = VedleggDAO(),
) {
    fun finnIdent(søknadId: String, txSession: TransactionalSession): String? {
        return txSession.run(
            queryOf(hentIdent, søknadId)
                .map { row -> row.toIdent() }
                .asSingle,
        )
    }

    fun finnJournalpostId(søknadId: String, txSession: TransactionalSession): String? {
        return txSession.run(
            queryOf(hentIdent, søknadId)
                .map { row -> row.toJournalpostId() }
                .asSingle,
        )
    }

    fun hent(innsendingId: InnsendingId, txSession: TransactionalSession): Søknad? {
        return txSession.run(
            queryOf(hent, innsendingId.toString())
                .map { row -> row.toSøknad(txSession) }
                .asSingle,
        )
    }

    fun lagre(innsendingId: InnsendingId, søknad: Søknad?, txSession: TransactionalSession) {
        søknad?.let { lagreHeleSøknaden(innsendingId, it, txSession) } // TODO: Burde vel egentlig slette søknaden..
    }

    private fun søknadFinnes(søknadId: SøknadId, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, søknadId.toString()).map { row -> row.boolean("exists") }.asSingle,
    ) ?: throw RuntimeException("Failed to check if søknad exists")

    // Søknaden vil aldri endres, så det er ingen grunn til å oppdatere den hvis den først har blitt lagret
    private fun lagreHeleSøknaden(innsendingId: InnsendingId, søknad: Søknad, txSession: TransactionalSession) {
        if (søknadFinnes(søknad.id, txSession)) {
            return
        }

        lagreSøknad(innsendingId, søknad, txSession)
        barnetilleggDAO.lagre(søknad.id, søknad.barnetillegg, txSession)
        tiltakDAO.lagre(søknad.id, søknad.tiltak, txSession)
        vedleggDAO.lagre(søknad.id, søknad.vedlegg, txSession)
    }

    private fun lagreSøknad(innsendingId: InnsendingId, søknad: Søknad, txSession: TransactionalSession) {
        val periodeSpm = listOf(
            "kvp" to søknad.kvp,
            "intro" to søknad.intro,
            "institusjon" to søknad.institusjon,
            "sykepenger" to søknad.sykepenger,
            "supplerendeAlder" to søknad.supplerendeStønadAlder,
            "supplerendeFlyktning" to søknad.supplerendeStønadFlyktning,
            "jobbsjansen" to søknad.jobbsjansen,
        ).flatMap {
            listOf(
                it.first to lagrePeriodeSpm(it.second),
                it.first + "Fom" to lagrePeriodeSpmFra(it.second),
                it.first + "Tom" to lagrePeriodeSpmTil(it.second),
            )
        }.associate {
            it.first to it.second
        }

        txSession.run(
            queryOf(
                lagreSøknad,
                periodeSpm +
                mapOf(
                    "id" to søknad.id.toString(),
                    "innsendingId" to innsendingId.toString(),
                    "eksternSoknadId" to søknad.søknadId,
                    "fornavn" to søknad.personopplysninger.fornavn,
                    "etternavn" to søknad.personopplysninger.etternavn,
                    "ident" to søknad.personopplysninger.ident,
                    "deltarKvp" to lagrePeriodeSpm(søknad.kvp),


                    "supplerendeFlyktning" to lagrePeriodeSpm(søknad.supplerendeStønadFlyktning),
                    "supplerendeFlyktningFom" to lagrePeriodeSpmFra(søknad.supplerendeStønadFlyktning),
                    "supplerendeFlyktningTom" to lagrePeriodeSpmTil(søknad.supplerendeStønadFlyktning),

                    "jobbsjansen" to lagrePeriodeSpm(søknad.jobbsjansen),
                    "jobbsjansenFom" to lagrePeriodeSpmFra(søknad.jobbsjansen),
                    "jobbsjansenTom" to lagrePeriodeSpmTil(søknad.jobbsjansen),

                    "journalpostId" to søknad.journalpostId,
                    "dokumentinfoId" to søknad.dokumentInfoId,
                    "opprettet" to søknad.innsendt,
                    "tidsstempelHosOss" to søknad.tidsstempelHosOss,
                ),
            ).asUpdate,
        )
    }

    private fun lagrePeriodeSpmFra(periodeSpm: PeriodeSpm) = when (periodeSpm) {
        is PeriodeSpm.Ja -> periodeSpm.periode.fra
        is PeriodeSpm.Nei -> null
        is PeriodeSpm.IkkeRelevant -> null
        is PeriodeSpm.IkkeMedISøknaden -> null
    }

    private fun lagrePeriodeSpmTil(periodeSpm: PeriodeSpm) = when (periodeSpm) {
        is PeriodeSpm.Ja -> periodeSpm.periode.til
        is PeriodeSpm.Nei -> null
        is PeriodeSpm.IkkeRelevant -> null
        is PeriodeSpm.IkkeMedISøknaden -> null
    }

    private fun lagrePeriodeSpm(periodeSpm: PeriodeSpm) = when (periodeSpm) {
        is PeriodeSpm.Ja -> JA
        is PeriodeSpm.Nei -> NEI
        is PeriodeSpm.IkkeRelevant -> IKKE_RELEVANT
        is PeriodeSpm.IkkeMedISøknaden -> IKKE_MED_I_SØKNADEN
    }

    private fun Row.toIdent() = string("ident")

    private fun Row.toJournalpostId() = string("journalpost_id")

    private fun Row.toSøknad(txSession: TransactionalSession): Søknad {
        val id = SøknadId.fromDb(string("id"))
        val søknadId = string("søknad_id")
        val fornavn = string("fornavn")
        val etternavn = string("etternavn")
        val ident = string("ident")
        val deltarKvp = boolean("deltar_kvp")
        val kvpFom = localDateOrNull("kvp_fom")
        val kvpTom = localDateOrNull("kvp_tom")
        val deltarIntro = boolean("deltar_intro")
        val introFom = localDateOrNull("intro_fom")
        val introTom = localDateOrNull("intro_tom")
        val oppholdInstitusjon = booleanOrNull("institusjon_opphold")
        val typeInstitusjon = stringOrNull("institusjon_type")?.let { TypeInstitusjon.valueOf(it) }
        val opprettet = localDateTimeOrNull("opprettet")
        val tidsstempelHosOss = localDateTime("tidsstempel_hos_oss")
        val dokumentInfoId = string("dokumentinfo_id")
        val journalpostId = string("journalpost_id")
        val fritekst = stringOrNull("fritekst")
        val barnetillegg = barnetilleggDAO.hentBarnetilleggListe(id, txSession)
        val tiltak = tiltakDAO.hent(id, txSession)
        val vedlegg = vedleggDAO.hentVedleggListe(id, txSession)

        return Søknad(
            id = id,
            søknadId = søknadId,
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = fornavn,
                etternavn = etternavn,
            ),
            kvp =
            kvp = Søknad.Kvp(
                deltar = deltarKvp,
                periode = kvpFom?.let {
                    Periode(
                        fra = kvpFom,
                        til = kvpTom!!,
                    )
                },
            ),
            intro = PeriodeSpm(
                deltar = deltarIntro,
                periode = introFom?.let {
                    Periode(
                        fra = introFom,
                        til = introTom!!,
                    )
                },
            ),
            institusjon = oppholdInstitusjon,
            innsendt = opprettet,
            barnetillegg = barnetillegg,
            tidsstempelHosOss = tidsstempelHosOss,
            tiltak = tiltak,
            trygdOgPensjon = trygdOgPensjon,
            fritekst = fritekst,
            vedlegg = vedlegg,
        )
    }

    @Language("SQL")
    private val lagreSøknad = """
        insert into søknad (
            id,
            innsending_id,
            søknad_id,
            journalpost_id,
            dokumentinfo_id,
            versjon,
            fornavn, 
            etternavn, 
            ident, 
            deltar_kvp, 
            kvp_fom,
            kvp_tom,
            deltar_intro,
            intro_fom,
            intro_tom,
            institusjon_opphold, 
            institusjon_fom,
            institusjon_tom,
            etterlønn,
            gjenlevendepensjon,
            gjenlevendepensjonFom,
            alderspensjon,
            alderspensjonFom,
            sykepenger,
            sykepengerFom,
            sykepengerTom,
            supplerendeAlder,
            supplerendeAlderFom,
            supplerendeAlderTom,
            supplerendeFlyktning,
            supplerendeFlyktningFom,
            supplerendeFlyktningTom,
            jobbsjansen,
            jobbsjansenFom,
            jobbsjansenTom,
            privatpensjon,
            privatpensjonFom,
            innsendt,
            tidsstempel_hos_oss
        ) values (
            :id,
            :innsendingId,
            :eksternSoknadId,
            :journalpostId,
            :dokumentinfoId,            
            :versjon,
            :fornavn, 
            :etternavn,
            :ident,
            :deltarKvp,
            :kvpFom,
            :kvpTom,
            :deltarIntro,
            :introFom,
            :introTom,
            :instOpphold,
            :instFom,
            :instTom,
            :etterlonn,
            :gjenlevendepensjon,
            :gjenlevendepensjonFom,
            :alderspensjon,
            :alderspensjonFom,
            :sykepenger,
            :sykepengerFom,
            :sykepengerTom,
            :supplerendeAlder,
            :supplerendeAlderFom,
            :supplerendeAlderTom,
            :supplerendeFlyktning,
            :supplerendeFlyktningFom,
            :supplerendeFlyktningTom,
            :jobbsjansen,
            :jobbsjansenFom,
            :jobbsjansenTom,
            :privatpensjon,
            :privatpensjonFom,
            :innsendt,
            :tidsstempelHosOss
        )
    """.trimIndent()

    private val JA =
    @Language("SQL")
    private val finnes = "select exists(select 1 from søknad where id = ?)"

    @Language("SQL")
    private val hent = "select * from søknad where innsending_id = ?"

    @Language("SQL")
    private val hentIdent = "select * from søknad where søknad_id = ?"
}
