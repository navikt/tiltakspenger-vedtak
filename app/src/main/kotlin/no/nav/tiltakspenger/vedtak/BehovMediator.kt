package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime
import java.util.*
import mu.KLogger
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection


private val LOG = KotlinLogging.logger {}

class BehovMediator(
    private val rapidsConnection: RapidsConnection,
    private val sikkerLogg: KLogger
) {

    internal fun håndter(hendelse: Hendelse) {
        // Hvorfor ikke bare
        // hendelse.aktivitetslogg.let { if (!it.hasErrors()) håndter(hendelse, it.behov()) }
        // ?? Hva er det hendelse.kontekster() gjør som er så lurt?
        hendelse.kontekster().forEach { if (!it.hasErrors()) håndter(hendelse, it.behov()) }
    }

    private fun håndter(
        hendelse: Hendelse,
        behov: List<Aktivitetslogg.Aktivitet.Behov>
    ) {
        // Denne linja sørger for at alle behov som har lik kontekst (Map<String, String>) behandles sammen
        // og blir sendt ut som en og samme melding på Rapiden.
        // Hvorfor det nødvendigvis er riktig/viktig vet jeg ikke om jeg forstår..
        behov.groupBy { it.alleKonteksterAsMap() }.forEach { (kontekst, listeAvBehov) ->
            LOG.debug { "For kontekst $kontekst har vi følgende behov: $listeAvBehov" }
            val behovsliste = mutableListOf<String>()
            val id = UUID.randomUUID()

            mutableMapOf(
                "@event_name" to "behov",
                "@opprettet" to LocalDateTime.now(),
                "@id" to id,
                "@behov" to behovsliste
            )
                .apply {
                    putAll(kontekst)
                    listeAvBehov.forEach { etBehov ->
                        require(etBehov.type.name !in behovsliste) { "Kan ikke produsere samme behov ${etBehov.type.name} på samme kontekst" }
                        require(
                            etBehov.detaljer().filterKeys { this.containsKey(it) && this[it] != etBehov.detaljer()[it] }
                                .isEmpty()
                        ) { "Kan ikke produsere behov med duplikate detaljer" }
                        behovsliste.add(etBehov.type.name)
                        putAll(etBehov.detaljer())
                    }
                }
                .let { JsonMessage.newMessage(it) }
                .also { message ->
                    sikkerLogg.info { "Sender $id som ${message.toJson()}" }
                    //Midlertidig:
                    LOG.info { "Sender $id som ${message.toJson()}" }
                    rapidsConnection.publish(hendelse.ident(), message.toJson())
                    LOG.info { "Sender behov ${behovsliste.joinToString { it }}" }
                }
        }
    }
}