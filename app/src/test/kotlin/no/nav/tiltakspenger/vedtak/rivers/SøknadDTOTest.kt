package no.nav.tiltakspenger.vedtak.rivers

import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTO.Companion.mapSøknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class SøknadDTOTest {

    @Test
    fun map() {
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
            oppholdInstitusjon = true,
            typeInstitusjon = "fengsel",
            opprettet = tidsstempel,
            barnetillegg = listOf(
                BarnetilleggDTO(alder = 10, ident = "456", land = "NOR"),
                BarnetilleggDTO(alder = 13, fødselsdato = LocalDate.now(), land = "SWE")
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
                startdato = LocalDate.now()

            ),
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakskode = "JOBSOK",
                arrangoernavn = "Arrangør",
                beskrivelse = null,
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = null,
                postnummer = null,
                antallDager = 0
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjonDTO(utbetaler = "", prosent = null, fom = fom, tom = tom)
            ),
            fritekst = "hei"
        )

        val søknad = mapSøknad(søknadDTO, LocalDateTime.MIN)

        assertEquals(søknadDTO.søknadId, søknad.søknadId)
        assertEquals(søknadDTO.journalpostId, søknad.journalpostId)
        assertEquals(søknadDTO.dokumentInfoId, søknad.dokumentInfoId)
        assertEquals(søknadDTO.fornavn, søknad.fornavn)
        assertEquals(søknadDTO.etternavn, søknad.etternavn)
        assertEquals(søknadDTO.ident, søknad.ident)
        assertEquals(søknadDTO.deltarKvp, søknad.deltarKvp)
        assertEquals(søknadDTO.deltarIntroduksjonsprogrammet, søknad.deltarIntroduksjonsprogrammet)
        assertEquals(søknadDTO.oppholdInstitusjon, søknad.oppholdInstitusjon)
        assertEquals(søknadDTO.typeInstitusjon, søknad.typeInstitusjon)
        assertEquals(søknadDTO.opprettet, søknad.opprettet)
        assertEquals(søknadDTO.fritekst, søknad.fritekst)
        søknadDTO.barnetillegg.forEach { barnetilleggDTO ->
            søknad.barnetillegg.forAtLeastOne {
                it.land shouldBe barnetilleggDTO.land
                it.alder shouldBe barnetilleggDTO.alder
                it.fornavn shouldBe barnetilleggDTO.fornavn
                it.etternavn shouldBe barnetilleggDTO.etternavn
            }
            if (barnetilleggDTO.ident != null) {
                søknad.barnetillegg.forAtLeastOne {
                    it.shouldBeTypeOf<Barnetillegg.MedIdent>()
                    it.ident shouldBe barnetilleggDTO.ident
                }
            } else {
                søknad.barnetillegg.forAtLeastOne {
                    it.shouldBeTypeOf<Barnetillegg.UtenIdent>()
                    it.fødselsdato shouldBe barnetilleggDTO.fødselsdato
                }
            }
        }

    }
}
