package no.nav.tiltakspenger.vedtak.rivers

import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.tiltakspenger.felles.oktober
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TypeInstitusjon
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTO.Companion.mapSøknad
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTO.PersonopplysningerDTO
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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "annet",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "456", oppholdsland = "NOR"),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertEquals(søknadDTO.søknadId, søknad.søknadId)
        assertEquals(søknadDTO.journalpostId, søknad.journalpostId)
        assertEquals(søknadDTO.dokumentInfoId, søknad.dokumentInfoId)
        assertEquals(søknadDTO.personopplysninger.fornavn, søknad.personopplysninger.fornavn)
        assertEquals(søknadDTO.personopplysninger.etternavn, søknad.personopplysninger.etternavn)
        assertEquals(søknadDTO.personopplysninger.ident, søknad.personopplysninger.ident)
        assertEquals(søknadDTO.kvalifiseringsprogram.deltar, søknad.kvp.deltar)
        assertEquals(søknadDTO.introduksjonsprogram.deltar, søknad.intro.deltar)
        assertEquals(søknadDTO.oppholdInstitusjon, søknad.institusjon)
        assertEquals(TypeInstitusjon.ANNET, søknad.typeInstitusjon)
        assertEquals(søknadDTO.opprettet, søknad.innsendt)
        assertEquals(søknadDTO.fritekst, søknad.fritekst)
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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "annet",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "456", oppholdsland = "NOR"),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.intro.periode)
        assertEquals(søknadDTO.introduksjonsprogram.periode?.fra, søknad.intro.periode?.fra)
        assertEquals(søknadDTO.introduksjonsprogram.periode?.til, søknad.intro.periode?.til)
    }

    @Test
    fun mapBarnetilleggFelter() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "overgangsbolig",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "456", oppholdsland = "NOR"),
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
            trygdOgPensjon = listOf(),
            fritekst = "hei",
            vedlegg = null,
        )

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)
        søknadDTO.barnetillegg.forEach { barnetilleggDTO ->
            søknad.barnetillegg.forAtLeastOne {
                it.oppholdsland shouldBe barnetilleggDTO.oppholdsland
                it.alder shouldBe barnetilleggDTO.alder
                it.fornavn shouldBe barnetilleggDTO.fornavn
                it.etternavn shouldBe barnetilleggDTO.etternavn
            }
            if (barnetilleggDTO.ident != null) {
                søknad.barnetillegg.forAtLeastOne {
                    it.shouldBeTypeOf<Barnetillegg.FraPdl>()
                    it.fødselsdato shouldBe barnetilleggDTO.ident
                }
            } else {
                søknad.barnetillegg.forAtLeastOne {
                    it.shouldBeTypeOf<Barnetillegg.Manuell>()
                    it.fødselsdato shouldBe barnetilleggDTO.fødselsdato
                }
            }
        }
    }

    @Test
    fun mapArenaTiltak() {
        val tidsstempel = LocalDateTime.of(2022, Month.SEPTEMBER, 13, 15, 0)
        val søknadDTO = SøknadDTO(
            søknadId = "42",
            journalpostId = "43",
            dokumentInfoId = "44",
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.ArenaTiltak)
        assertEquals(søknadDTO.arenaTiltak!!.arenaId, (søknad.tiltak as Tiltak.ArenaTiltak).arenaId)
        assertEquals(søknadDTO.arenaTiltak!!.arrangoer, (søknad.tiltak as Tiltak.ArenaTiltak).arrangoernavn)
        assertEquals(
            søknadDTO.arenaTiltak!!.harSluttdatoFraArena,
            (søknad.tiltak as Tiltak.ArenaTiltak).harSluttdatoFraArena,
        )
        assertEquals(søknadDTO.arenaTiltak!!.erIEndreStatus, (søknad.tiltak as Tiltak.ArenaTiltak).erIEndreStatus)
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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertNotNull(søknad.tiltak)
        assertTrue(søknad.tiltak is Tiltak.ArenaTiltak)
        assertEquals(søknadDTO.arenaTiltak!!.arenaId, (søknad.tiltak as Tiltak.ArenaTiltak).arenaId)
        assertEquals(søknadDTO.arenaTiltak!!.arrangoer, (søknad.tiltak as Tiltak.ArenaTiltak).arrangoernavn)
        assertEquals(
            søknadDTO.arenaTiltak!!.harSluttdatoFraArena,
            (søknad.tiltak as Tiltak.ArenaTiltak).harSluttdatoFraArena,
        )
        assertEquals(søknadDTO.arenaTiltak!!.erIEndreStatus, (søknad.tiltak as Tiltak.ArenaTiltak).erIEndreStatus)
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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

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
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Ola",
                etternavn = "Nordmann",
                ident = "123",
            ),
            kvalifiseringsprogram = SøknadDTO.KvalifiseringsprogramDTO(
                deltar = false,
                periode = null,
            ),
            introduksjonsprogram = SøknadDTO.IntroduksjonsprogramDTO(
                deltar = true,
                SøknadDTO.PeriodeDTO(
                    fra = 1.oktober(2022),
                    til = 10.oktober(2022),
                ),
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = "barneverninstitusjon",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "456", oppholdsland = "NOR"),
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

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        søknadDTO.trygdOgPensjon!!.forEach { trygdOgPensjonDTO ->
            søknad.trygdOgPensjon.forAtLeastOne {
                it.utbetaler shouldBe trygdOgPensjonDTO.utbetaler
                it.prosent shouldBe trygdOgPensjonDTO.prosent
                it.fom shouldBe trygdOgPensjonDTO.fom
                it.tom shouldBe trygdOgPensjonDTO.tom
            }
        }
    }
}
