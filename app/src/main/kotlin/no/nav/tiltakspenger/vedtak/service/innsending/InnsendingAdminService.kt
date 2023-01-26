package no.nav.tiltakspenger.vedtak.service.innsending

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

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

    fun hentInnsendingerSomErFerdigstilt() =
        innsendingRepository.hentInnsendingerMedTilstandFerdigstilt()
}
