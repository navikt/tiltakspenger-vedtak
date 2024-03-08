package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.behandling.Søknad

class SøknadMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val søknad: Søknad,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun søknad() = søknad
}
