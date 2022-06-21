package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse

class JoarkHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val hendelseType: String,
    private val journalpostStatus: String,
    private val behandlingstema: String? = null
) : Hendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
}