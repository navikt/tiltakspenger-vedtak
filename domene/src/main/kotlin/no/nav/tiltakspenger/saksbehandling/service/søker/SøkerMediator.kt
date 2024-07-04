package no.nav.tiltakspenger.saksbehandling.service.søker

import no.nav.tiltakspenger.innsending.domene.ISøkerHendelse

/**
 * Tilgjengeliggjør SøkerMediator for andre servicer, så vi får en mykere overgang fra RnR til synkrone kall.
 * TODO jah: Skal slettes når vi tar ned RnR.
 */
interface SøkerMediator {
    fun håndter(hendelse: ISøkerHendelse)
}
