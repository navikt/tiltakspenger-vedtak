package no.nav.tiltakspenger.vedtak.repository.innsending

import no.nav.tiltakspenger.innsending.Innsending
import no.nav.tiltakspenger.innsending.InnsendingTilstandType
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

class InMemoryInnsendingRepository : InnsendingRepository {

    private val innsendinger = mutableMapOf<String, Innsending>()

    override fun hent(journalpostId: String): Innsending? = innsendinger[journalpostId]

    override fun lagre(innsending: Innsending): Innsending = with(innsendinger) {
        this[innsending.journalpostId] = innsending
        return innsending
    }

    override fun findByIdent(ident: String): List<Innsending> = emptyList()

    override fun antall(): Long = innsendinger.size.toLong()

    override fun antallMedTilstandFaktainnhentingFeilet(): Long =
        innsendinger.values.filter { it.tilstand.type == InnsendingTilstandType.FaktainnhentingFeilet }.size.toLong()

    override fun antallStoppetUnderBehandling(): Long =
        innsendinger.values.filter {
            it.tilstand.type != InnsendingTilstandType.FaktainnhentingFeilet &&
                it.tilstand.type != InnsendingTilstandType.InnsendingFerdigstilt
        }.size.toLong()

    override fun hentInnsendingerMedTilstandFaktainnhentingFeilet(): List<String> {
        return innsendinger.values
            .filter { it.tilstand.type == InnsendingTilstandType.FaktainnhentingFeilet }
            .map { it.journalpostId }
    }

    override fun hentInnsendingerMedTilstandFerdigstilt(): List<String> {
        return innsendinger.values
            .filter { it.tilstand.type == InnsendingTilstandType.InnsendingFerdigstilt }
            .map { it.journalpostId }
    }

    override fun hentInnsendingerStoppetUnderBehandling(): List<String> {
        return innsendinger.values
            .filter {
                it.tilstand.type != InnsendingTilstandType.FaktainnhentingFeilet &&
                    it.tilstand.type != InnsendingTilstandType.InnsendingFerdigstilt
            }
            .map { it.journalpostId }
    }

    fun reset() = innsendinger.clear()
}
