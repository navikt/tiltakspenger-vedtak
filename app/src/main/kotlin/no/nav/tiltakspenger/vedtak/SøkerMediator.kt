package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class SøkerMediator(
    private val søkerRepository: SøkerRepository,
    private val observatører: List<SøkerObserver> = emptyList(),
    rapidsConnection: RapidsConnection
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection
    )

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        håndter(søknadMottattHendelse) { søker ->
            søker.håndter(søknadMottattHendelse)
        }
    }

    fun håndter(persondataMottattHendelse: PersondataMottattHendelse) {
        håndter(persondataMottattHendelse) { søker ->
            søker.håndter(persondataMottattHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, handler: (Søker) -> Unit) {
        try {
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
        return when (val søker = søkerRepository.hent(hendelse.ident())) {
            is Søker -> {
                SECURELOG.debug { "Fant Søker for ${hendelse.ident()}" }
                søker
            }

            else -> {
                val nySøker = Søker(hendelse.ident())
                søkerRepository.lagre(nySøker)
                SECURELOG.info { "Opprettet Søker for ${hendelse.ident()}" }
                nySøker
            }
        }
    }

    private fun finalize(søker: Søker, hendelse: Hendelse) {
        søkerRepository.lagre(søker)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) {
            LOG.warn("aktivitetslogg inneholder errors, se securelog for detaljer")
            SECURELOG.warn("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        } else {
            LOG.info("aktivitetslogg inneholder meldinger, se securelog for detaljer")
            SECURELOG.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
            behovMediator.håndter(hendelse)
        }
    }
}