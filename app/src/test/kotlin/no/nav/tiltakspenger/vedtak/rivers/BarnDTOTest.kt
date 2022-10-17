package no.nav.tiltakspenger.vedtak.rivers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

internal class BarnDTOTest {
    companion object {
        @Suppress("UnusedPrivateMember")
        @JvmStatic
        private fun providesAlderForBarnetilllegg() = listOf(
            Arguments.of(LocalDate.now(), true),
            Arguments.of(LocalDate.now().minusYears(5), true),
            Arguments.of(LocalDate.now().minusYears(ALDER_BARNETILLEGG), true),
            Arguments.of(
                LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR).plusDays(1), true
            ),
            Arguments.of(LocalDate.now().minusYears(20), false),
            Arguments.of(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR), false),
            Arguments.of(
                LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR).minusDays(1), false
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("providesAlderForBarnetilllegg")
    fun kanGiRettPåBarnetillegg(fødselsdato: LocalDate, expectedResult: Boolean) {
        val barn = BarnDTO(
            ident = "",
            fornavn = "",
            mellomnavn = null,
            etternavn = "",
            fødselsdato = LocalDate.now(),
            adressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT
        )

        assertEquals(expectedResult, barn.copy(fødselsdato = fødselsdato).kanGiRettPåBarnetillegg())
    }
}
