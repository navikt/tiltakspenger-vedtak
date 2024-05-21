package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode

class SuperSaksopplysning<T> (
    val saksopplysningerSaksbehandler: FaktiskSaksopplysning<T>,
    val saksopplysningerAnnet: FaktiskSaksopplysning<T>,
) {
    fun avklarFakta() {}
}

data class FaktiskSaksopplysning<T>(
    val kilde: Kilde,
    val saksopplysning: T,
    val periode: Periode,
    val saksbehandler: String?,
)

data class SaksopplysningPeriode<T>(
    val saksopplysning: T,
)

// class testting{
//    fun testest() {
//        val antallDager = SuperSaksopplysning<Int>(saksopplysningerSaksbehandler = )
//    }
// }
