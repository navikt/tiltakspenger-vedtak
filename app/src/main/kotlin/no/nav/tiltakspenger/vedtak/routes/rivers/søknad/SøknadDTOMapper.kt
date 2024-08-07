package no.nav.tiltakspenger.vedtak.routes.rivers.søknad

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Barnetillegg
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.SøknadsTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Vedlegg
import java.time.LocalDateTime

object SøknadDTOMapper {
    fun mapSøknad(
        dto: SøknadDTO,
        innhentet: LocalDateTime,
    ): Søknad =
        Søknad(
            id = SøknadId.fromString(dto.søknadId),
            versjon = dto.versjon,
            journalpostId = dto.dokInfo.journalpostId,
            dokumentInfoId = dto.dokInfo.dokumentInfoId,
            filnavn = dto.dokInfo.filnavn,
            personopplysninger =
            Søknad.Personopplysninger(
                fornavn = dto.personopplysninger.fornavn,
                etternavn = dto.personopplysninger.etternavn,
                fnr = Fnr.fromString(dto.personopplysninger.ident),
            ),
            tiltak = mapTiltak(dto.tiltak),
            barnetillegg =
            dto.barnetilleggPdl.map { mapBarnetilleggPDL(it) } +
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
        )

    private fun mapPeriodeSpm(periodeSpmDTO: PeriodeSpmDTO): Søknad.PeriodeSpm =
        when (periodeSpmDTO.svar) {
            SpmSvarDTO.Nei -> Søknad.PeriodeSpm.Nei
            SpmSvarDTO.Ja ->
                Søknad.PeriodeSpm.Ja(
                    periode =
                    Periode(
                        fraOgMed = periodeSpmDTO.fom!!,
                        tilOgMed = periodeSpmDTO.tom!!,
                    ),
                )
        }

    private fun mapFraOgMedSpm(fraOgMedDatoSpmDTO: FraOgMedDatoSpmDTO): Søknad.FraOgMedDatoSpm {
        if (fraOgMedDatoSpmDTO.svar == SpmSvarDTO.Ja && fraOgMedDatoSpmDTO.fom == null) {
            throw IllegalStateException("Det skal ikke være mulig med null i fradato hvis man har svart JA")
        }
        return when (fraOgMedDatoSpmDTO.svar) {
            SpmSvarDTO.Nei -> Søknad.FraOgMedDatoSpm.Nei
            SpmSvarDTO.Ja ->
                Søknad.FraOgMedDatoSpm.Ja(
                    fra = fraOgMedDatoSpmDTO.fom!!,
                )
        }
    }

    private fun mapTiltak(dto: SøknadsTiltakDTO): SøknadsTiltak =
        SøknadsTiltak(
            id = dto.id,
            deltakelseFom = dto.deltakelseFom,
            deltakelseTom = dto.deltakelseTom,
            arrangør = dto.arrangør,
            typeKode = dto.typeKode,
            typeNavn = dto.typeNavn,
        )

    private fun mapVedlegg(dto: DokumentInfoDTO): Vedlegg =
        Vedlegg(
            journalpostId = dto.journalpostId,
            dokumentInfoId = dto.dokumentInfoId,
            filnavn = dto.filnavn,
        )

    private fun mapBarnetilleggManuelle(dto: BarnetilleggDTO): Barnetillegg.FraPdl =
        Barnetillegg.FraPdl(
            oppholderSegIEØS = mapJaNei(dto.oppholderSegIEØS),
            fornavn = dto.fornavn!!,
            mellomnavn = dto.mellomnavn,
            etternavn = dto.etternavn!!,
            fødselsdato = dto.fødselsdato!!,
        )

    private fun mapBarnetilleggPDL(dto: BarnetilleggDTO): Barnetillegg.FraPdl =
        Barnetillegg.FraPdl(
            oppholderSegIEØS = mapJaNei(dto.oppholderSegIEØS),
            fornavn = dto.fornavn,
            mellomnavn = dto.mellomnavn,
            etternavn = dto.etternavn,
            fødselsdato = dto.fødselsdato!!,
        )

    private fun mapJaNei(jaNeiSpmDTO: JaNeiSpmDTO): Søknad.JaNeiSpm =
        when (jaNeiSpmDTO.svar) {
            SpmSvarDTO.Nei -> Søknad.JaNeiSpm.Nei
            SpmSvarDTO.Ja -> Søknad.JaNeiSpm.Ja
        }
}
