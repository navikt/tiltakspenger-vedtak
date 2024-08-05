package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

/*
* Denne skal bygges på! Men i første omgang trenger vi kun å vite om
* bruker har oppgitt at hen går på noen livsoppholdytelser
*
*/

fun Søknad.livsoppholdSaksopplysning(vurderingsperiode: Periode): LivsoppholdSaksopplysning.Søknad {
    return LivsoppholdSaksopplysning.Søknad(
        harLivsoppholdYtelser = harLivsoppholdYtelser(),
        tidsstempel = LocalDateTime.now(),
        periode = vurderingsperiode,
    )
}
