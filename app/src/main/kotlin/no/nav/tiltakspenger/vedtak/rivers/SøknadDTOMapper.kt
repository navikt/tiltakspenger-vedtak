package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.Vedlegg
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SøknadDTOMapper {

    fun mapSøknad(dto: SøknadDTO, innhentet: LocalDateTime): Søknad {
        return Søknad(
            versjon = dto.versjon,
            søknadId = dto.søknadId,
            journalpostId = dto.dokInfo.journalpostId,
            dokumentInfoId = dto.dokInfo.dokumentInfoId,
            filnavn = dto.dokInfo.filnavn,
            personopplysninger = Søknad.Personopplysninger(
                fornavn = dto.personopplysninger.fornavn,
                etternavn = dto.personopplysninger.etternavn,
                ident = dto.personopplysninger.ident,
            ),
            tiltak = mapArenatiltak(dto.arenaTiltak)
                ?: mapBrukerregistrertTiltak(dto.brukerTiltak),
            barnetillegg = dto.barnetilleggPdl.map { mapBarnetilleggPDL(it) } +
                dto.barnetilleggManuelle.map { mapBarnetilleggManuelle(it) },
            opprettet = dto.opprettet,
            tidsstempelHosOss = innhentet,
            vedlegg = dto.vedlegg.map { mapVedlegg(it) },
            kvp = mapPeriodeSpm(dto.kvp),
            intro = mapPeriodeSpm(dto.intro),
            institusjon = mapPeriodeSpm(dto.institusjon),
            etterlønn = mapJaNei(dto.etterlønn),
            gjenlevendepensjon = mapPeriodeSpm(dto.gjenlevendepensjon),
            alderspensjon = mapFraOgMedSpm(dto.alderspensjon),
            sykepenger = mapPeriodeSpm(dto.sykepenger),
            supplerendeStønadAlder = mapPeriodeSpm(dto.supplerendeStønadAlder),
            supplerendeStønadFlyktning = mapPeriodeSpm(dto.supplerendeStønadFlyktning),
            jobbsjansen = mapPeriodeSpm(dto.jobbsjansen),
            trygdOgPensjon = mapPeriodeSpm(dto.trygdOgPensjon),
            lønnetArbeid = mapJaNei(dto.lønnetArbeid),
        )
    }

    private fun mapPeriodeSpm(periodeSpmDTO: PeriodeSpmDTO): Søknad.PeriodeSpm {
        return when (periodeSpmDTO.svar) {
            SpmSvarDTO.IkkeMedISøknaden -> Søknad.PeriodeSpm.IkkeMedISøknaden
            SpmSvarDTO.IkkeRelevant -> Søknad.PeriodeSpm.IkkeRelevant
            SpmSvarDTO.IkkeBesvart -> Søknad.PeriodeSpm.IkkeBesvart
            SpmSvarDTO.FeilaktigBesvart -> Søknad.PeriodeSpm.FeilaktigBesvart(
                svartJa = true,
                fom = periodeSpmDTO.fom,
                tom = periodeSpmDTO.tom,
            )

            SpmSvarDTO.Nei -> Søknad.PeriodeSpm.Nei
            SpmSvarDTO.Ja -> Søknad.PeriodeSpm.Ja(
                periode = Periode(
                    fra = periodeSpmDTO.fom!!,
                    til = periodeSpmDTO.tom!!,
                ),
            )
        }
    }

    private fun mapFraOgMedSpm(fraOgMedDatoSpmDTO: FraOgMedDatoSpmDTO): Søknad.FraOgMedDatoSpm {
        val dato = fraOgMedDatoSpmDTO.fom ?: LocalDate.of(1970, 1, 1)
        val fraDato = if (dato.isBefore(LocalDate.of(1970, 1, 1))) {
            LocalDate.of(1970, 1, 1)
        } else {
            dato
        }
        return when (fraOgMedDatoSpmDTO.svar) {
            SpmSvarDTO.IkkeMedISøknaden -> Søknad.FraOgMedDatoSpm.IkkeMedISøknaden
            SpmSvarDTO.IkkeRelevant -> Søknad.FraOgMedDatoSpm.IkkeRelevant
            SpmSvarDTO.IkkeBesvart -> Søknad.FraOgMedDatoSpm.IkkeBesvart
            SpmSvarDTO.FeilaktigBesvart -> Søknad.FraOgMedDatoSpm.FeilaktigBesvart(
                svartJa = true,
                fom = fraDato,
            )

            SpmSvarDTO.Nei -> Søknad.FraOgMedDatoSpm.Nei
            SpmSvarDTO.Ja -> Søknad.FraOgMedDatoSpm.Ja(
                fra = fraDato,
            )
        }
    }

    private fun mapArenatiltak(dto: ArenaTiltakDTO?): Tiltak.ArenaTiltak? = if (dto == null) {
        null
    } else {
        val startDato = dto.opprinneligStartdato ?: LocalDate.of(1970, 1, 1)
        val startDatoHack = if (startDato.isBefore(LocalDate.of(1970, 1, 1))) {
            LocalDate.of(1970, 1, 1)
        } else {
            startDato
        }
        Tiltak.ArenaTiltak(
            arenaId = dto.arenaId,
            arrangoernavn = dto.arrangoernavn,
            // TODO hack for å sette default tiltakskode for de nye som mangler
            tiltakskode = try {
                Tiltaksaktivitet.Tiltak.valueOf(
                    dto.tiltakskode.uppercase(),
                )
            } catch (t: Throwable) {
                Tiltaksaktivitet.Tiltak.UKJENT_ELLER_IKKE_OPPGITT
            },
            opprinneligSluttdato = dto.opprinneligSluttdato,
            opprinneligStartdato = startDatoHack,
            sluttdato = dto.sluttdato,
            startdato = dto.startdato,
        )
    }

    private fun mapBrukerregistrertTiltak(dto: BrukerTiltakDTO?): Tiltak.BrukerregistrertTiltak? =
        if (dto == null) {
            null
        } else {
            Tiltak.BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.mapTiltaksType(dto.tiltakskode), // TODO:test
                arrangoernavn = dto.arrangoernavn,
                beskrivelse = dto.beskrivelse,
                startdato = dto.fom,
                sluttdato = dto.tom,
                adresse = dto.adresse,
                postnummer = dto.postnummer,
                antallDager = dto.antallDager,
            )
        }

    private fun mapVedlegg(dto: DokumentInfoDTO): Vedlegg {
        return Vedlegg(
            journalpostId = dto.journalpostId,
            dokumentInfoId = dto.dokumentInfoId,
            filnavn = dto.filnavn,
        )
    }

    private fun mapBarnetilleggManuelle(dto: BarnetilleggDTO): Barnetillegg.FraPdl {
        return Barnetillegg.FraPdl(
            oppholderSegIEØS = mapJaNei(dto.oppholderSegIEØS),
            fornavn = dto.fornavn!!,
            mellomnavn = dto.mellomnavn,
            etternavn = dto.etternavn!!,
            fødselsdato = dto.fødselsdato!!,
        )
    }

    private fun mapBarnetilleggPDL(dto: BarnetilleggDTO): Barnetillegg.FraPdl {
        return Barnetillegg.FraPdl(
            oppholderSegIEØS = mapJaNei(dto.oppholderSegIEØS),
            fornavn = dto.fornavn!!,
            mellomnavn = dto.mellomnavn,
            etternavn = dto.etternavn!!,
            fødselsdato = dto.fødselsdato!!,
        )
    }

    fun mapJaNei(jaNeiSpmDTO: JaNeiSpmDTO): Søknad.JaNeiSpm {
        return when (jaNeiSpmDTO.svar) {
            SpmSvarDTO.IkkeMedISøknaden -> Søknad.JaNeiSpm.IkkeMedISøknaden
            SpmSvarDTO.IkkeRelevant -> Søknad.JaNeiSpm.IkkeRelevant
            SpmSvarDTO.IkkeBesvart -> Søknad.JaNeiSpm.IkkeBesvart
            SpmSvarDTO.FeilaktigBesvart -> Søknad.JaNeiSpm.IkkeBesvart
            SpmSvarDTO.Nei -> Søknad.JaNeiSpm.Nei
            SpmSvarDTO.Ja -> Søknad.JaNeiSpm.Ja
        }
    }

    private fun toFødselsdato(ident: String): LocalDate =
        LocalDate.parse(ident.subSequence(0, 6), DateTimeFormatter.ofPattern("ddMMuu"))
}
