package no.nav.tiltakspenger.vedtak.service.innsending

import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
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

    fun hentInnsendingerSomErFerdigstilt(): List<String> =
        innsendingRepository.hentInnsendingerMedTilstandFerdigstilt().also {
            LOG.info { "Fant ${it.size} innsendinger i tilstand ferdigstilt" }
        }
}
