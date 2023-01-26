package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse

class InnsendingUtdatertHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
}
