package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.innsending.domene.Innsending
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.InnsendingObserver
import no.nav.tiltakspenger.innsending.domene.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.ForeldrepengerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.OvergangsstønadMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.UføreMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.innsending.ports.InnsendingRepository
import org.slf4j.MDC
import kotlin.system.measureTimeMillis

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class InnsendingMediatorImpl(
    private val innsendingRepository: InnsendingRepository,
    private val observatører: List<InnsendingObserver> = emptyList(),
    rapidsConnection: RapidsConnection,
) : InnsendingMediator {

    private val behovMediator: BehovMediator = BehovMediator(
        rapidsConnection = rapidsConnection,
    )

    override fun håndter(hendelse: InnsendingHendelse) {
        val elapsed = measureTimeMillis {
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
                            is TiltakMottattHendelse -> innsending.håndter(hendelse)
                            is YtelserMottattHendelse -> innsending.håndter(hendelse)
                            is PersonopplysningerMottattHendelse -> innsending.håndter(hendelse)
                            is SkjermingMottattHendelse -> innsending.håndter(hendelse)
                            is ForeldrepengerMottattHendelse -> innsending.håndter(hendelse)
                            is OvergangsstønadMottattHendelse -> innsending.håndter(hendelse)
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
        LOG.info { "InnsendingMediator.håndter tok $elapsed ms for ${hendelse::class.java.simpleName}" }
    }

    private fun hentEllerOpprettInnsending(hendelse: SøknadMottattHendelse): Innsending {
        return when (val innsending = innsendingRepository.hent(hendelse.journalpostId())) {
            is Innsending -> {
                SECURELOG.debug { "Fant Innsending for ${hendelse.journalpostId()}" }
                innsending
            }

            else -> {
                val nyInnsending = Innsending(
                    journalpostId = hendelse.journalpostId(),
                    ident = hendelse.søknad().personopplysninger.ident,
                    fom = hendelse.søknad().tiltak.deltakelseFom,
                    tom = hendelse.søknad().tiltak.deltakelseTom,
                )
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
