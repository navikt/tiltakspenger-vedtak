package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class SøkerMediator(
    private val søkerRepository: SøkerRepository,
    private val observatører: List<SøkerObserver> = emptyList(),
    rapidsConnection: RapidsConnection
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
        sikkerLogg = sikkerlogg
    )

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        håndter(søknadMottattHendelse) { søker ->
            søker.håndter(søknadMottattHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, handler: (Søker) -> Unit) {
        try {
            MDC.put("ident", hendelse.ident())
            hentEllerOpprettSøker(hendelse).also { søker ->
                observatører.forEach { søker.addObserver(it) }
                handler(søker)
                finalize(søker, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun hentEllerOpprettSøker(hendelse: Hendelse): Søker {
        val søker = søkerRepository.hent(hendelse.ident())
        return when (søker) {
            is Søker -> {
                LOG.debug { "Fant Søker for ${hendelse.ident()}" }
                søker
            }
            else -> {
                val nySøker = Søker(hendelse.ident())
                søkerRepository.lagre(nySøker)
                LOG.info { "Opprettet Søker for ${hendelse.ident()}" }
                nySøker
            }
        }
    }

    private fun finalize(søker: Søker, hendelse: Hendelse) {
        søkerRepository.lagre(søker)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) return sikkerlogg.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        behovMediator.håndter(hendelse)
    }
}