package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

/*
* Denne skal bygges på! Men i første omgang trenger vi kun å vite om
* bruker har oppgitt at hen går på noen livsoppholdytelser
*
*/

fun Søknad.livsoppholdSaksopplysning(vurderingsperiode: Periode): LivsoppholdSaksopplysning.Søknad =
    LivsoppholdSaksopplysning.Søknad(
        harLivsoppholdYtelser = harLivsoppholdYtelser(),
        tidsstempel = nå(),
        periode = vurderingsperiode,
    )
