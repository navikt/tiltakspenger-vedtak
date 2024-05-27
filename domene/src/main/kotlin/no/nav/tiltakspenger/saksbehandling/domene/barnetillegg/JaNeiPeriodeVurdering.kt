package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall

interface JaNeiPeriodeVurdering {

    fun vurdering(): Periodisering<Utfall>
}
