package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse

class InnsendingUtdatertHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
}
