package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse

data class Stønadsdager(
    val vurderingsperiode: Periode,
    val registerSaksopplysning: StønadsdagerSaksopplysning.Register,
) {
    val lovreferanse = Lovreferanse.STØNADSDAGER
    val logger = KotlinLogging.logger { }

    fun krymp(nyPeriode: Periode): Stønadsdager {
        if (vurderingsperiode == nyPeriode) return this
        require(vurderingsperiode.inneholderHele(nyPeriode)) { "Ny periode ($nyPeriode) må være innenfor vurderingsperioden ($vurderingsperiode)" }
        return this.copy(
            vurderingsperiode = nyPeriode,
            registerSaksopplysning = registerSaksopplysning.krymp(nyPeriode),
        )
    }

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
