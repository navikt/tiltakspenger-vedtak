package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Søknad

class SøknadMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val søknad: Søknad,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun søknad() = søknad
}
