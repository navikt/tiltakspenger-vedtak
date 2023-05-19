package no.nav.tiltakspenger.vedtak.repository.søknad

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlettMegTest {

    @Test
    fun slettMeg() {
        val map = mapOf(
            "test" to Søknad.PeriodeSpm.Ja(Periode(LocalDate.EPOCH, LocalDate.now())),
            "test2" to Søknad.PeriodeSpm.Nei,
        ).toParametersForPeriodeSpm()
        println(map)
    }

    fun Map<String, Søknad.PeriodeSpm>.toParametersForPeriodeSpm(): Map<String, Any> =
        this.flatMap { (k, v) ->
            listOf(
                k to lagrePeriodeSpm(v),
                k + "Fom" to lagrePeriodeSpmFra(v),
                k + "Tom" to lagrePeriodeSpmTil(v),
            )
        }.associate {
            it.first to it.second as Any
        }

    private fun lagrePeriodeSpmFra(periodeSpm: Søknad.PeriodeSpm) = when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> periodeSpm.periode.fra
        is Søknad.PeriodeSpm.Nei -> null
        is Søknad.PeriodeSpm.IkkeRelevant -> null
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> null
    }

    private fun lagrePeriodeSpmTil(periodeSpm: Søknad.PeriodeSpm) = when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> periodeSpm.periode.til
        is Søknad.PeriodeSpm.Nei -> null
        is Søknad.PeriodeSpm.IkkeRelevant -> null
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> null
    }

    private fun lagrePeriodeSpm(periodeSpm: Søknad.PeriodeSpm) = when (periodeSpm) {
        is Søknad.PeriodeSpm.Ja -> JA
        is Søknad.PeriodeSpm.Nei -> NEI
        is Søknad.PeriodeSpm.IkkeRelevant -> IKKE_RELEVANT
        is Søknad.PeriodeSpm.IkkeMedISøknaden -> IKKE_MED_I_SØKNADEN
    }

    private val JA = "Ja"
    private val NEI = "Nei"
    private val IKKE_RELEVANT = "IkkeRelevant"
    private val IKKE_MED_I_SØKNADEN = "IkkeMedISøknaden"


}
