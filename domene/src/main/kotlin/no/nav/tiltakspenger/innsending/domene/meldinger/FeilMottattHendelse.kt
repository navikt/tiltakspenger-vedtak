package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.Feil
import no.nav.tiltakspenger.innsending.domene.ISøkerHendelse
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse

class FeilMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val feil: Feil,
) : InnsendingHendelse(aktivitetslogg), ISøkerHendelse {

    override fun journalpostId() = journalpostId
    override fun ident() = ident

    fun feil() = feil
}
