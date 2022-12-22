package no.nav.tiltakspenger.vedtak.service.innsending

import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository


private val LOG = KotlinLogging.logger {}

class InnsendingAdminService(
    private val innsendingRepository: InnsendingRepository,
    private val innsendingMediator: InnsendingMediator
) {

    fun resettInnsendingerSomHarFeiletEllerStoppetOpp() {
        val innsendinger = innsendingRepository.hentInnsendingerMedTilstandFaktainnhentingFeilet() +
                innsendingRepository.hentInnsendingerStoppetUnderBehandling()
        innsendinger.forEach {
            innsendingMediator.håndter(
                ResetInnsendingHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = it,
                )
            )
        }
    }

    fun resettInnsendingerSomErFerdigstilt() {
        LOG.info { "Henter alle innsendinger med tilstand ferdigstilt" }
        val innsendinger = innsendingRepository.hentInnsendingerMedTilstandFerdigstilt()
        LOG.info { "Fant ${innsendinger.count()} innsendinger" }
        innsendinger.forEach {
            innsendingMediator.håndter(
                ResetInnsendingHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = it,
                )
            )
        }
        LOG.info { "Har resatt alle" }
    }
}
