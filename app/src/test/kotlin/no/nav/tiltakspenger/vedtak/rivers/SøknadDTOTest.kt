package no.nav.tiltakspenger.vedtak.rivers

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
                BarnetilleggDTO(alder = 13, ident = "789", land = "SWE")
            ),
            arenaTiltak = ArenaTiltakDTO(
                arenaId = "7",
                arrangoer = "Arrangør",
                harSluttdatoFraArena = false,
                tiltakskode = "JOBBK",
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = null,
                sluttdato = null,
                startdato = null

            ),
            brukerregistrertTiltak = BrukerregistrertTiltakDTO(
                tiltakstype = "JOBSOK",
                arrangoernavn = null,
                beskrivelse = null,
                fom = null,
                tom = null,
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
    }
}
