package no.nav.tiltakspenger.vedtak.service.innsending

import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

private val LOG = KotlinLogging.logger {}

class InnsendingAdminService(
    private val innsendingRepository: InnsendingRepository,
    private val innsendingMediator: InnsendingMediator,
) {

    fun resettInnsendingerSomHarFeiletEllerStoppetOpp() {
        val innsendinger =
            innsendingRepository.hentInnsendingerMedTilstandFaktainnhentingFeilet()
                .also { LOG.info { "Fant ${it.size} innsendinger i tilstand feilet" } } +
                innsendingRepository.hentInnsendingerStoppetUnderBehandling()
                    .also { LOG.info { "Fant ${it.size} innsendinger i tilstand stoppet eller under behandling" } }
        innsendinger.forEach {
            innsendingMediator.håndter(
                ResetInnsendingHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = it,
                ),
            )
        }
    }

    fun hentInnsendingerSomHarFeiletEllerStoppetOpp(): AdminInnsendingerDAO {
        val innsendingerFeilet = innsendingRepository.hentInnsendingerMedTilstandFaktainnhentingFeilet().also {
            LOG.info { "Fant ${it.size} innsendinger i tilstand feilet" }
        }
        val innsendingerStoppet = innsendingRepository.hentInnsendingerStoppetUnderBehandling().also {
            LOG.info { "Fant ${it.size} innsendinger i tilstand stoppet eller under behandling" }
        }
        return AdminInnsendingerDAO(innsendingerFeilet.size, innsendingerStoppet.size)
    }

    fun hentInnsendingerSomErFerdigstilt(): List<String> =
        innsendingRepository.hentInnsendingerMedTilstandFerdigstilt().also {
            LOG.info { "Fant ${it.size} innsendinger i tilstand ferdigstilt" }
        }

    // @TODO Flytt denne, hører ikke hjemme her.
    data class AdminInnsendingerDAO(
        val feilet: Int,
        val stoppet: Int,
    )
}
