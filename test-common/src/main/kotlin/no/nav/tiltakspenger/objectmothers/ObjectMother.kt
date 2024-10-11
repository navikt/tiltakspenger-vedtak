package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn

object ObjectMother :
    SaksbehandlerMother,
    SøknadMother,
    BehandlingMother,
    SakMother,
    VilkårMother,
    PersonMother,
    MeldekortMother,
    UtbetalingsvedtakMother {
    fun navn() = Navn("Fornavn", "Mellomnavn", "Etternavn")
    fun navkontor() = Navkontor("0220")
}
