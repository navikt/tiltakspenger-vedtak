package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.innsending.domene.ISøkerHendelse
import no.nav.tiltakspenger.innsending.domene.Søker
import no.nav.tiltakspenger.innsending.domene.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerMediator
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

/**
 * TODO jah: Skal slettes når vi tar ned RnR.
 */
class SøkerMediatorImpl(
    private val søkerRepository: SøkerRepository,
    rapidsConnection: RapidsConnection,
) : SøkerMediator {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
    )

    override fun håndter(hendelse: ISøkerHendelse) {
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
