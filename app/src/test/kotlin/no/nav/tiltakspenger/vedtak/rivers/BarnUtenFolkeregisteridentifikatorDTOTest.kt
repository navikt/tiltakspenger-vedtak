package no.nav.tiltakspenger.vedtak.rivers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BarnUtenFolkeregisteridentifikatorDTOTest {
    @Test
    fun kanGiRettPåBarnetillegg() {
        val barn = BarnUtenFolkeregisteridentifikatorDTO(
            fornavn = null,
            mellomnavn = null,
            etternavn = null,
            fødselsdato = null
        )

        assertTrue(barn.kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now()).kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now().minusYears(5)).kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now().minusYears(16)).kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now().minusYears(16).plusDays(1)).kanGiRettPåBarnetillegg())
        assertFalse(barn.copy(fødselsdato = LocalDate.now().minusYears(16).minusDays(2)).kanGiRettPåBarnetillegg())
        assertFalse(barn.copy(fødselsdato = LocalDate.now().minusYears(20)).kanGiRettPåBarnetillegg())
    }
}
