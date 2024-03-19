package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse

class InnsendingUtdatertHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
}
