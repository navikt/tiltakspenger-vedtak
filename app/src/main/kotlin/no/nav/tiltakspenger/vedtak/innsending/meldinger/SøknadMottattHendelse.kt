package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse

class SøknadMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val søknad: Søknad,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun søknad() = søknad
}
