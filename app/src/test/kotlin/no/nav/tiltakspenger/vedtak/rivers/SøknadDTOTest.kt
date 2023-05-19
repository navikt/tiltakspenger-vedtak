package no.nav.tiltakspenger.vedtak.rivers

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import no.nav.tiltakspenger.felles.oktober
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class SøknadDTOTest {

    @Test
    fun mapBasisFelter() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val fom = LocalDate.of(2022, Month.SEPTEMBER, 12)
        val tom = LocalDate.of(2022, Month.SEPTEMBER, 14)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "Barneverninstitusjon",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(
                    fornavn = "Ola",
                    etternavn = "Hansen",
                    alder = 10,
                    ident = "10101012345",
                    oppholdsland = "NOR",
                ),
                BarnetilleggDTO(
                    fornavn = "Petter",
                    etternavn = "Davidsen",
                    alder = 13,
                    fødselsdato = LocalDate.now(),
                    oppholdsland = "SWE",
                ),
            ),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),
            ),
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakskode = "JOBSOK",
                arrangoernavn = "Arrangør",
                beskrivelse = null,
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = null,
                postnummer = null,
                antallDager = 0,
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjonDTO(utbetaler = "", prosent = null, fom = fom, tom = tom),
            ),
            fritekst = "hei",
            vedlegg = listOf(
                VedleggDTO(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentInfoId",
                    filnavn = "filnavn",
                ),
            ),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertEquals(søknadDTO.søknadId, søknad.søknadId)
        assertEquals(søknadDTO.journalpostId, søknad.journalpostId)
        assertEquals(søknadDTO.dokumentInfoId, søknad.dokumentInfoId)
        assertEquals(søknadDTO.fornavn, søknad.personopplysninger.fornavn)
        assertEquals(søknadDTO.etternavn, søknad.personopplysninger.etternavn)
        assertEquals(søknadDTO.ident, søknad.personopplysninger.ident)

        assertTrue(søknad.kvp is Søknad.PeriodeSpm.Nei)
        assertTrue(søknad.intro is Søknad.PeriodeSpm.Ja)
        assertTrue(søknad.institusjon is Søknad.PeriodeSpm.Ja)
        assertEquals(søknadDTO.opprettet, søknad.opprettet)
        assertEquals(søknadDTO.vedlegg?.first()?.journalpostId, søknad.vedlegg.first().journalpostId)
        assertEquals(søknadDTO.vedlegg?.first()?.dokumentInfoId, søknad.vedlegg.first().dokumentInfoId)
        assertEquals(søknadDTO.vedlegg?.first()?.filnavn, søknad.vedlegg.first().filnavn)
    }

    @Test
    fun mapIntroduksjonsprogrammetDetaljer() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val fom = LocalDate.of(2022, Month.SEPTEMBER, 12)
        val tom = LocalDate.of(2022, Month.SEPTEMBER, 14)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "annet",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(
                    fornavn = "Ola",
                    etternavn = "Hansen",
                    alder = 10,
                    ident = "10101012345",
                    oppholdsland = "NOR",
                ),
                BarnetilleggDTO(
                    fornavn = "Petter",
                    etternavn = "Davidsen",
                    alder = 13,
                    fødselsdato = LocalDate.now(),
                    oppholdsland = "SWE",
                ),
            ),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),

            ),
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakskode = "JOBSOK",
                arrangoernavn = "Arrangør",
                beskrivelse = null,
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = null,
                postnummer = null,
                antallDager = 0,
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjonDTO(utbetaler = "", prosent = null, fom = fom, tom = tom),
            ),
            fritekst = "hei",
            vedlegg = emptyList(),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)
        assertTrue(søknad.intro is Søknad.PeriodeSpm.Ja)
        val spm = søknad.intro as Søknad.PeriodeSpm.Ja
        assertEquals(søknadDTO.introduksjonsprogrammetDetaljer?.fom, spm.periode.fra)
        assertEquals(søknadDTO.introduksjonsprogrammetDetaljer?.tom, spm.periode.til)
    }

    @Test
    fun mapBarnetilleggFelter() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "overgangsbolig",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(
                    fornavn = "Ola",
                    etternavn = "Hansen",
                    alder = 10,
                    ident = "10101012345",
                    oppholdsland = "NOR",
                ),
                BarnetilleggDTO(
                    fornavn = "Petter",
                    etternavn = "Davidsen",
                    alder = 13,
                    fødselsdato = LocalDate.now(),
                    oppholdsland = "SWE",
                ),
            ),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),

            ),
            brukerregistrertTiltak = null,
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = null,
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)
        søknad.barnetillegg shouldContainExactlyInAnyOrder listOf(
            Barnetillegg.FraPdl(
                oppholderSegIEØS = Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = "Ola",
                mellomnavn = null,
                etternavn = "Hansen",
                fødselsdato = 10.oktober(2010),
            ),
            Barnetillegg.Manuell(
                oppholderSegIEØS = Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = "Petter",
                mellomnavn = null,
                etternavn = "Davidsen",
                fødselsdato = LocalDate.now(),
            ),
        )
    }

    @Test
    fun mapArenaTiltak() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "overgangsbolig",
            opprettet = tidsstempel,
            barnetillegg = listOf(),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),

            ),
            brukerregistrertTiltak = null,
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = null,
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.ArenaTiltak)
        assertEquals(søknadDTO.arenaTiltak!!.arenaId, (søknad.tiltak as Tiltak.ArenaTiltak).arenaId)
        assertEquals(søknadDTO.arenaTiltak!!.arrangoer, (søknad.tiltak as Tiltak.ArenaTiltak).arrangoernavn)
        assertEquals(Tiltaksaktivitet.Tiltak.JOBBK, (søknad.tiltak as Tiltak.ArenaTiltak).tiltakskode)
        assertEquals(søknadDTO.arenaTiltak!!.startdato, (søknad.tiltak as Tiltak.ArenaTiltak).startdato)
        assertEquals(søknadDTO.arenaTiltak!!.sluttdato, (søknad.tiltak as Tiltak.ArenaTiltak).sluttdato)
        assertEquals(
            søknadDTO.arenaTiltak!!.opprinneligStartdato,
            (søknad.tiltak as Tiltak.ArenaTiltak).opprinneligStartdato,
        )
        assertEquals(
            søknadDTO.arenaTiltak!!.opprinneligSluttdato,
            (søknad.tiltak as Tiltak.ArenaTiltak).opprinneligSluttdato,
        )
    }

    @Test
    fun mapArenaTiltakMedAMO() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "overgangsbolig",
            opprettet = tidsstempel,
            barnetillegg = listOf(),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "AMO",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),

            ),
            brukerregistrertTiltak = null,
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = emptyList(),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.ArenaTiltak)
        assertEquals(søknadDTO.arenaTiltak!!.arenaId, (søknad.tiltak as Tiltak.ArenaTiltak).arenaId)
        assertEquals(søknadDTO.arenaTiltak!!.arrangoer, (søknad.tiltak as Tiltak.ArenaTiltak).arrangoernavn)
        assertEquals(Tiltaksaktivitet.Tiltak.AMO, (søknad.tiltak as Tiltak.ArenaTiltak).tiltakskode)
        assertEquals(søknadDTO.arenaTiltak!!.startdato, (søknad.tiltak as Tiltak.ArenaTiltak).startdato)
        assertEquals(søknadDTO.arenaTiltak!!.sluttdato, (søknad.tiltak as Tiltak.ArenaTiltak).sluttdato)
        assertEquals(
            søknadDTO.arenaTiltak!!.opprinneligStartdato,
            (søknad.tiltak as Tiltak.ArenaTiltak).opprinneligStartdato,
        )
        assertEquals(
            søknadDTO.arenaTiltak!!.opprinneligSluttdato,
            (søknad.tiltak as Tiltak.ArenaTiltak).opprinneligSluttdato,
        )
    }

    @Test
    fun mapBrukerregistrertTiltak() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "barneverninstitusjon",
            opprettet = tidsstempel,
            barnetillegg = listOf(),
            arenaTiltak = null,
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakskode = "JOBSOK",
                arrangoernavn = "Arrangør",
                beskrivelse = "beskrivelse",
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = "Tiltaksveien",
                postnummer = "0489",
                antallDager = 4,
            ),
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = emptyList(),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.BrukerregistrertTiltak)
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.adresse,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).adresse,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.arrangoernavn,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).arrangoernavn,
        )
        assertEquals(Tiltaksaktivitet.Tiltak.JOBBK, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).tiltakskode)
        assertEquals(søknadDTO.brukerregistrertTiltak!!.fom, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).startdato)
        assertEquals(søknadDTO.brukerregistrertTiltak!!.tom, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).sluttdato)
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.beskrivelse,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).beskrivelse,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.postnummer,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).postnummer,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.antallDager,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).antallDager,
        )
    }

    @Test
    fun mapBrukerregistrertTiltakAMO() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "barneverninstitusjon",
            opprettet = tidsstempel,
            barnetillegg = listOf(),
            arenaTiltak = null,
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakskode = "AMO",
                arrangoernavn = "Arrangør",
                beskrivelse = "beskrivelse",
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = "Tiltaksveien",
                postnummer = "0489",
                antallDager = 4,
            ),
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = emptyList(),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.BrukerregistrertTiltak)
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.adresse,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).adresse,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.arrangoernavn,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).arrangoernavn,
        )
        assertEquals(Tiltaksaktivitet.Tiltak.GRUPPEAMO, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).tiltakskode)
        assertEquals(søknadDTO.brukerregistrertTiltak!!.fom, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).startdato)
        assertEquals(søknadDTO.brukerregistrertTiltak!!.tom, (søknad.tiltak as Tiltak.BrukerregistrertTiltak).sluttdato)
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.beskrivelse,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).beskrivelse,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.postnummer,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).postnummer,
        )
        assertEquals(
            søknadDTO.brukerregistrertTiltak!!.antallDager,
            (søknad.tiltak as Tiltak.BrukerregistrertTiltak).antallDager,
        )
    }

    @Test
    fun mapTrygdOgPensjon() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val fom = LocalDate.of(2022, Month.SEPTEMBER, 12)
        val tom = LocalDate.of(2022, Month.SEPTEMBER, 14)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            fornavn = "Ola",
            etternavn = "Nordmann",
            ident = "123",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljerDTO(
                fom = LocalDate.of(2022, 10, 1),
                tom = LocalDate.of(2022, 10, 10),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "barneverninstitusjon",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "10101012345", oppholdsland = "NOR"),
                BarnetilleggDTO(alder = 13, fødselsdato = LocalDate.now(), oppholdsland = "SWE"),
            ),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),

            ),
            brukerregistrertTiltak = null,
            trygdOgPensjon = listOf(
                TrygdOgPensjonDTO(utbetaler = "Storebrand", prosent = 50, fom = fom, tom = tom),
                TrygdOgPensjonDTO(utbetaler = "Storebrand", prosent = 100, fom = null, tom = null),
                TrygdOgPensjonDTO(utbetaler = "Storebrand", prosent = null, fom = null, tom = null),
            ),
            fritekst = "hei",
            vedlegg = emptyList(),
        )

        val søknad = SøknadDTOMapper.mapSøknad(søknadDTO, LocalDateTime.MIN)
        assertTrue(søknad.etterlønn is Søknad.JaNeiSpm.Ja)
    }
}
