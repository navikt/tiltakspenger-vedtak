package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

/*
* Denne skal bygges på! Men i første omgang trenger vi kun å vite om
* bruker har oppgitt at hen går på noen livsoppholdytelser
*
*/

fun Søknad.livsoppholdSaksopplysning(vurderingsPeriode: Periode): LivsoppholdSaksopplysning {
    val harLivsoppholdYtelserISøknad =
        fraPeriodeSpm(sykepenger) ||
            fraJaNeiSpm(etterlønn) ||
            fraPeriodeSpm(trygdOgPensjon) ||
            fraPeriodeSpm(gjenlevendepensjon) ||
            fraPeriodeSpm(supplerendeStønadAlder) ||
            fraPeriodeSpm(supplerendeStønadFlyktning) ||
            fraOgMedDatoSpm(alderspensjon) ||
            fraPeriodeSpm(jobbsjansen) ||
            fraPeriodeSpm(trygdOgPensjon)

    return LivsoppholdSaksopplysning.Søknad(
        harLivsoppholdYtelser = harLivsoppholdYtelserISøknad,
        // TODO kew: Setter denne til null siden det ikke skal med i første omgang
        årsakTilEndring = null,
        tidsstempel = LocalDateTime.now(),
        periode = vurderingsPeriode,
    )
}

private fun fraPeriodeSpm(spm: Søknad.PeriodeSpm): Boolean {
    return when (spm) {
        is Søknad.PeriodeSpm.Ja -> true
        Søknad.PeriodeSpm.Nei -> false
    }
}

private fun fraJaNeiSpm(spm: Søknad.JaNeiSpm): Boolean {
    return when (spm) {
        Søknad.JaNeiSpm.Ja -> true
        Søknad.JaNeiSpm.Nei -> false
    }
}

private fun fraOgMedDatoSpm(spm: Søknad.FraOgMedDatoSpm): Boolean {
    return when (spm) {
        is Søknad.FraOgMedDatoSpm.Ja -> true
        Søknad.FraOgMedDatoSpm.Nei -> false
    }
}
