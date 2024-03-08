package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse

class InnsendingUtdatertHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
}
