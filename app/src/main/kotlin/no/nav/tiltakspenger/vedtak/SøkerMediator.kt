package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.innsending.ISøkerHendelse
import no.nav.tiltakspenger.innsending.Søker
import no.nav.tiltakspenger.innsending.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SøkerMediator(
    private val søkerRepository: SøkerRepository,
    rapidsConnection: RapidsConnection,
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
    )

    fun håndter(hendelse: ISøkerHendelse) {
        try {
            hentEllerOpprettSøker(hendelse).also { søker ->
                when (hendelse) {
                    is IdentMottattHendelse -> søker.håndter(hendelse)
                    is PersonopplysningerMottattHendelse -> søker.håndter(hendelse)
                    is SkjermingMottattHendelse -> søker.håndter(hendelse)
                    else -> throw RuntimeException("Ukjent hendelse")
                }
                finalize(søker, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun hentEllerOpprettSøker(hendelse: ISøkerHendelse): Søker {
        return when (val søker = søkerRepository.findByIdent(hendelse.ident())) {
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

    private fun finalize(søker: Søker, hendelse: ISøkerHendelse) {
        søkerRepository.lagre(søker)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) {
            LOG.warn("aktivitetslogg inneholder errors, se securelog for detaljer")
            SECURELOG.warn("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        } else {
            LOG.info("aktivitetslogg inneholder meldinger, se securelog for detaljer")
            SECURELOG.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        }
    }
}
