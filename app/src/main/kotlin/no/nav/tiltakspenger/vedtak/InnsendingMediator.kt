package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class InnsendingMediator(
    private val innsendingRepository: InnsendingRepository,
    private val observatører: List<InnsendingObserver> = emptyList(),
    rapidsConnection: RapidsConnection
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection
    )

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        håndter(søknadMottattHendelse) { innsending ->
            innsending.håndter(søknadMottattHendelse)
        }
    }

    fun håndter(personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
        håndter(personopplysningerMottattHendelse) { innsending ->
            innsending.håndter(personopplysningerMottattHendelse)
        }
    }

    fun håndter(skjermingMottattHendelse: SkjermingMottattHendelse) {
        håndter(skjermingMottattHendelse) { innsending ->
            innsending.håndter(skjermingMottattHendelse)
        }
    }

    fun håndter(arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
        håndter(arenaTiltakMottattHendelse) { innsending ->
            innsending.håndter(arenaTiltakMottattHendelse)
        }
    }

    fun håndter(ytelserMottattHendelse: YtelserMottattHendelse) {
        håndter(ytelserMottattHendelse) { innsending ->
            innsending.håndter(ytelserMottattHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, handler: (Innsending) -> Unit) {
        try {
            hentEllerOpprettInnsending(hendelse).also { innsending ->
                observatører.forEach { innsending.addObserver(it) }
                handler(innsending)
                finalize(innsending, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun hentEllerOpprettInnsending(hendelse: Hendelse): Innsending {
        return when (val innsending = innsendingRepository.hent(hendelse.journalpostId())) {
            is Innsending -> {
                SECURELOG.debug { "Fant Innsending for ${hendelse.journalpostId()}" }
                innsending
            }

            else -> {
                val nyInnsending = Innsending(hendelse.journalpostId())
                innsendingRepository.lagre(nyInnsending)
                SECURELOG.info { "Opprettet Innsending for ${hendelse.journalpostId()}" }
                nyInnsending
            }
        }
    }

    private fun finalize(innsending: Innsending, hendelse: Hendelse) {
        innsendingRepository.lagre(innsending)
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
