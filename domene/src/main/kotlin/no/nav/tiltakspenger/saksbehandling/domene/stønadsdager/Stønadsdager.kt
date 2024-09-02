package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse

/**
 * TODO pre-mvp jah: Snakk med Benedicte og spør om dette er litt overkill for kun å ha antall dager per uke på tiltaket?
 */
data class Stønadsdager(
    val vurderingsperiode: Periode,
    val registerSaksopplysning: StønadsdagerSaksopplysning.Register,
) {
    val lovreferanse = Lovreferanse.STØNADSDAGER
    val logger = KotlinLogging.logger { }

    init {
        check(vurderingsperiode == registerSaksopplysning.periode) { "Vurderingsperioden må være lik perioden for antall dager" }
    }

    companion object {
        fun opprett(
            vurderingsperiode: Periode,
            registerSaksopplysning: StønadsdagerSaksopplysning.Register,
        ): Stønadsdager =
            Stønadsdager(
                vurderingsperiode = vurderingsperiode,
                registerSaksopplysning = registerSaksopplysning,
            )

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            registerSaksopplysning: StønadsdagerSaksopplysning.Register,
            vurderingsperiode: Periode,
        ): Stønadsdager =
            Stønadsdager(
                registerSaksopplysning = registerSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            )
    }
}
