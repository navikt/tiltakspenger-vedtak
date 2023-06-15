package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class SøknadDTOTest {

    @Test
    fun mapBasisFelter() {
        val søknadDTO = søknadDTO()
        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertEquals(søknadDTO.søknadId, søknad.søknadId)
        assertEquals(søknadDTO.dokInfo.journalpostId, søknad.journalpostId)
        assertEquals(søknadDTO.dokInfo.dokumentInfoId, søknad.dokumentInfoId)
        assertEquals(søknadDTO.dokInfo.filnavn, søknad.filnavn)
        assertEquals(søknadDTO.personopplysninger.fornavn, søknad.personopplysninger.fornavn)
        assertEquals(søknadDTO.personopplysninger.etternavn, søknad.personopplysninger.etternavn)
        assertEquals(søknadDTO.personopplysninger.ident, søknad.personopplysninger.ident)
        assertEquals(søknadDTO.opprettet, søknad.opprettet)
        assertEquals(søknadDTO.vedlegg.first().journalpostId, søknad.vedlegg.first().journalpostId)
        assertEquals(søknadDTO.vedlegg.first().dokumentInfoId, søknad.vedlegg.first().dokumentInfoId)
        assertEquals(søknadDTO.vedlegg.first().filnavn, søknad.vedlegg.first().filnavn)

        assertEquals(søknad.kvp, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.intro, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.institusjon, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.jobbsjansen, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.supplerendeStønadAlder, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.supplerendeStønadFlyktning, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.sykepenger, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.alderspensjon, Søknad.FraOgMedDatoSpm.Nei)
        assertEquals(søknad.gjenlevendepensjon, Søknad.PeriodeSpm.Nei)
        assertEquals(søknad.trygdOgPensjon, Søknad.JaNeiSpm.Nei)
        assertEquals(søknad.etterlønn, Søknad.JaNeiSpm.Nei)
    }

    @Test
    fun `ja i alt`() {
        val fra = LocalDate.of(2023, 1, 1)
        val til = LocalDate.of(2023, 12, 31)
        val søknadDTO = søknadDTO(
            kvp = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            intro = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            institusjon = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            jobbsjansen = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            supplerendeAlder = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            supplerendeFlykting = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            sykepenger = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            alderspensjon = FraOgMedDatoSpmDTO(svar = SpmSvarDTO.Ja, fom = fra),
            gjenlevendePensjon = PeriodeSpmDTO(svar = SpmSvarDTO.Ja, fom = fra, tom = til),
            trygdOgPensjon = JaNeiSpmDTO(svar = SpmSvarDTO.Ja),
            etterlønn = JaNeiSpmDTO(SpmSvarDTO.Ja),
        )
        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertEquals(søknad.kvp, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.intro, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.institusjon, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.jobbsjansen, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.supplerendeStønadAlder, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.supplerendeStønadFlyktning, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.sykepenger, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.alderspensjon, Søknad.FraOgMedDatoSpm.Ja(fra = fra))
        assertEquals(søknad.gjenlevendepensjon, Søknad.PeriodeSpm.Ja(Periode(fra = fra, til = til)))
        assertEquals(søknad.trygdOgPensjon, Søknad.JaNeiSpm.Ja)
        assertEquals(søknad.etterlønn, Søknad.JaNeiSpm.Ja)
    }

    private fun søknadDTO(
        fra: LocalDate = LocalDate.of(2023, 1, 1),
        til: LocalDate = LocalDate.of(2023, 12, 31),
        versjon: String = "1",
        søknadId: String = "42",
        dokInfo: DokumentInfoDTO = DokumentInfoDTO(
            journalpostId = "43",
            dokumentInfoId = "44",
            filnavn = "filnavn",
        ),
        personopplysninger: PersonopplysningerDTO = PersonopplysningerDTO(
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
        ),
        kvp: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        intro: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        institusjon: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        barnetilleggPdl: List<BarnetilleggDTO> = emptyList(),
        barnetilleggManuelle: List<BarnetilleggDTO> = emptyList(),
        opprettet: LocalDateTime = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0),
        arenaTiltak: ArenaTiltakDTO = ArenaTiltakDTO(
            arenaId = "arenaId",
            arrangoernavn = "Arrangørnavn",
            tiltakskode = "AMO",
            opprinneligSluttdato = til,
            opprinneligStartdato = fra,
            sluttdato = til,
            startdato = fra,
        ),
        brukerTiltak: BrukerTiltakDTO = BrukerTiltakDTO(
            tiltakskode = "AMO",
            arrangoernavn = "Arrangørnavn",
            beskrivelse = "Beskrivelse",
            fom = fra,
            tom = til,
            adresse = "Adresse",
            postnummer = "0111",
            antallDager = 2,
        ),
        alderspensjon: FraOgMedDatoSpmDTO = FraOgMedDatoSpmDTO(svar = SpmSvarDTO.Nei, fom = null),
        etterlønn: JaNeiSpmDTO = JaNeiSpmDTO(SpmSvarDTO.Nei),
        gjenlevendePensjon: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        jobbsjansen: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        supplerendeAlder: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        supplerendeFlykting: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        sykepenger: PeriodeSpmDTO = PeriodeSpmDTO(svar = SpmSvarDTO.Nei, fom = null, tom = null),
        trygdOgPensjon: JaNeiSpmDTO = JaNeiSpmDTO(svar = SpmSvarDTO.Nei),
        vedlegg: List<DokumentInfoDTO> = listOf(
            DokumentInfoDTO(
                journalpostId = "journalpostId",
                dokumentInfoId = "dokumentInfoId",
                filnavn = "filnavn",
            ),
        ),
    ) = SøknadDTO(
        versjon = versjon,
        søknadId = søknadId,
        dokInfo = dokInfo,
        personopplysninger = personopplysninger,
        kvp = kvp,
        intro = intro,
        institusjon = institusjon,
        barnetilleggPdl = barnetilleggPdl,
        barnetilleggManuelle = barnetilleggManuelle,
        arenaTiltak = arenaTiltak,
        brukerTiltak = brukerTiltak,
        alderspensjon = alderspensjon,
        etterlønn = etterlønn,
        gjenlevendepensjon = gjenlevendePensjon,
        jobbsjansen = jobbsjansen,
        supplerendeStønadAlder = supplerendeAlder,
        supplerendeStønadFlyktning = supplerendeFlykting,
        sykepenger = sykepenger,
        trygdOgPensjon = trygdOgPensjon,
        opprettet = opprettet,
        vedlegg = vedlegg,
    )
}
