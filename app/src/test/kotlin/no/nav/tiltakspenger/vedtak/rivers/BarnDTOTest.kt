package no.nav.tiltakspenger.vedtak.rivers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BarnDTOTest {
    @Test
    fun kanGiRettPåBarnetillegg() {
        val barn = BarnDTO(
            ident = "",
            fornavn = "",
            mellomnavn = null,
            etternavn = "",
            fødselsdato = LocalDate.now(),
            adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT
        )

        assertTrue(barn.copy(fødselsdato = LocalDate.now()).kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now().minusYears(5)).kanGiRettPåBarnetillegg())
        assertTrue(barn.copy(fødselsdato = LocalDate.now().minusYears(ALDER_BARNETILLEGG)).kanGiRettPåBarnetillegg())
        assertTrue(
            barn.copy(fødselsdato = LocalDate.now().minusYears(ALDER_BARNETILLEGG).plusDays(1))
                .kanGiRettPåBarnetillegg()
        )
        assertFalse(
            barn.copy(fødselsdato = LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusDays(2))
                .kanGiRettPåBarnetillegg()
        )
        assertFalse(barn.copy(fødselsdato = LocalDate.now().minusYears(20)).kanGiRettPåBarnetillegg())
    }
}
