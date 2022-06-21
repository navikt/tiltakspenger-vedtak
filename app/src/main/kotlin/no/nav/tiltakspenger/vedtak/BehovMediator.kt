package no.nav.tiltakspenger.vedtak

import mu.KLogger
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDateTime
import java.util.*


private val logg = KotlinLogging.logger {}

class BehovMediator(
    private val rapidsConnection: RapidsConnection,
    private val sikkerLogg: KLogger
) {

    internal fun håndter(hendelse: Hendelse) {
        hendelse.kontekster().forEach { if (!it.hasErrors()) håndter(hendelse, it.behov()) }
    }

    private fun håndter(
        hendelse: Hendelse,
        behov: List<Aktivitetslogg.Aktivitet.Behov>
    ) {
        behov.groupBy { it.kontekst() }.forEach { (kontekst, behov) ->
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
                    behov.forEach { behov ->
                        require(behov.type.name !in behovsliste) { "Kan ikke produsere samme behov ${behov.type.name} på samme kontekst" }
                        require(
                            behov.detaljer().filterKeys { this.containsKey(it) && this[it] != behov.detaljer()[it] }
                                .isEmpty()
                        ) { "Kan ikke produsere behov med duplikate detaljer" }
                        behovsliste.add(behov.type.name)
                        putAll(behov.detaljer())
                    }
                }
                .let { JsonMessage.newMessage(it) }
                .also { message ->
                    sikkerLogg.info { "Sender $id som ${message.toJson()}" }
                    rapidsConnection.publish(hendelse.journalpostId(), message.toJson())
                    logg.info { "Sender behov ${behovsliste.joinToString { it }}" }
                }
        }
    }
}