package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ForeldrepengerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.UføreMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import org.slf4j.MDC

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class InnsendingMediator(
    private val innsendingRepository: InnsendingRepository,
    private val observatører: List<InnsendingObserver> = emptyList(),
    rapidsConnection: RapidsConnection,
) {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
    )

    fun håndter(hendelse: InnsendingHendelse) {
        try {
            if (hendelse is SøknadMottattHendelse) {
                hentEllerOpprettInnsending(hendelse).also { innsending ->
                    observatører.forEach { innsending.addObserver(it) }
                    innsending.håndter(hendelse)
                    finalize(innsending, hendelse)
                }
            } else {
                hentInnsendingEllerFeil(hendelse)?.let { innsending ->
                    observatører.forEach { innsending.addObserver(it) }
                    when (hendelse) {
                        is ArenaTiltakMottattHendelse -> innsending.håndter(hendelse)
                        is YtelserMottattHendelse -> innsending.håndter(hendelse)
                        is PersonopplysningerMottattHendelse -> innsending.håndter(hendelse)
                        is SkjermingMottattHendelse -> innsending.håndter(hendelse)
                        is ForeldrepengerMottattHendelse -> innsending.håndter(hendelse)
                        is UføreMottattHendelse -> innsending.håndter(hendelse)
                        is ResetInnsendingHendelse -> innsending.håndter(hendelse)
                        is FeilMottattHendelse -> innsending.håndter(hendelse)
                        is InnsendingUtdatertHendelse -> innsending.håndter(hendelse)
                        else -> throw RuntimeException("Ukjent hendelse")
                    }
                    finalize(innsending, hendelse)
                }
            }
        } finally {
            MDC.clear()
        }
    }

    private fun hentEllerOpprettInnsending(hendelse: SøknadMottattHendelse): Innsending {
        return when (val innsending = innsendingRepository.hent(hendelse.journalpostId())) {
            is Innsending -> {
                SECURELOG.debug { "Fant Innsending for ${hendelse.journalpostId()}" }
                innsending
            }

            else -> {
                val nyInnsending = Innsending(hendelse.journalpostId(), hendelse.søknad().ident)
                innsendingRepository.lagre(nyInnsending)
                SECURELOG.info { "Opprettet Innsending for ${hendelse.journalpostId()}" }
                nyInnsending
            }
        }
    }

    private fun hentInnsendingEllerFeil(hendelse: InnsendingHendelse): Innsending? {
        return when (val innsending = innsendingRepository.hent(hendelse.journalpostId())) {
            is Innsending -> {
                SECURELOG.debug { "Fant Innsending for ${hendelse.journalpostId()}" }
                innsending
            }

            else -> {
                LOG.warn(
                    "Fant ingen innsending for hendelse med" +
                        "journalpostId ${hendelse.journalpostId()}, ignorerer hendelsen",
                )
                null
            }
        }
    }

    private fun finalize(innsending: Innsending, hendelse: InnsendingHendelse) {
        if (innsending.isDirty()) innsendingRepository.lagre(innsending)
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
