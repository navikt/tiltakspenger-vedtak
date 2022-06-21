package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.meldinger.JoarkHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import org.slf4j.MDC

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class InnsendingMediator(
    private val innsendingRepository: InnsendingRepository,
    private val observatører: List<InnsendingObserver> = emptyList(),
    rapidsConnection: RapidsConnection
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
        sikkerLogg = sikkerlogg
    )

    fun håndter(joarkHendelse: JoarkHendelse) {
        håndter(joarkHendelse) { innsending ->
            innsending.håndter(joarkHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, handler: (Innsending) -> Unit) {
        try {
            MDC.put("journalpostId", hendelse.journalpostId())
            innsending(hendelse).also { innsending ->
                observatører.forEach { innsending.addObserver(it) }
                handler(innsending)
                finalize(innsending, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun innsending(hendelse: Hendelse): Innsending {
        val innsending = innsendingRepository.hent(hendelse.journalpostId())
        return when (innsending) {
            is Innsending -> {
                log.debug { "Fant Innsending for ${hendelse.journalpostId()}" }
                innsending
            }
            else -> {
                val nyInnsending = Innsending(hendelse.journalpostId())
                innsendingRepository.lagre(nyInnsending)
                log.info { "Opprettet Innsending for ${hendelse.journalpostId()}" }
                nyInnsending
            }
        }
    }

    private fun finalize(innsending: Innsending, hendelse: Hendelse) {
        innsendingRepository.lagre(innsending)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) return sikkerlogg.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        behovMediator.håndter(hendelse)
    }
}