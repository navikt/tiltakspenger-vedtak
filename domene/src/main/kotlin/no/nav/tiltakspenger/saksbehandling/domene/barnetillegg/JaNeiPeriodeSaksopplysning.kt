package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class JaNeiPeriodeSaksopplysning(
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String,
    val verdi: Periodisering<JaNei?>,
) : SaksopplysningTrengerJegDenne {
    fun oppdaterVurderingsperiode(nyVurderingsperiode: Periode): JaNeiPeriodeSaksopplysning {
        return this.copy(verdi = verdi.utvid(nyVurderingsperiode))
    }
}
