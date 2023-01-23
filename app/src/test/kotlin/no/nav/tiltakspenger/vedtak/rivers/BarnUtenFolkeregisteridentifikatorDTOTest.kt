package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.libs.person.BarnUtenFolkeregisteridentifikator
import no.nav.tiltakspenger.vedtak.routes.rivers.ALDER_BARNETILLEGG
import no.nav.tiltakspenger.vedtak.routes.rivers.SIKKERHETSMARGIN_ÅR
import no.nav.tiltakspenger.vedtak.routes.rivers.kanGiRettPåBarnetillegg
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

internal class BarnUtenFolkeregisteridentifikatorDTOTest {
    companion object {

        @JvmStatic
        private fun providesAlderForBarnetilllegg() = listOf(
            Arguments.of(null, true),
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
    fun kanGiRettPåBarnetillegg(fødselsdato: LocalDate?, expectedResult: Boolean) {
        val barn = BarnUtenFolkeregisteridentifikator(
            fornavn = null,
            mellomnavn = null,
            etternavn = null,
            fødselsdato = null,
            statsborgerskap = null,
        )

        Assertions.assertEquals(expectedResult, barn.copy(fødselsdato = fødselsdato).kanGiRettPåBarnetillegg())
    }
}
