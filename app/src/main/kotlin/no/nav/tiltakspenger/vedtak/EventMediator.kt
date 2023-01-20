package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.routes.rivers.DayHasBegunEvent
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import java.time.LocalDateTime
import java.util.UUID

private val SECURELOG = KotlinLogging.logger("tjenestekall")

class EventMediator(
    private val rapidsConnection: RapidsConnection,
    private val innsendingAdminService: InnsendingAdminService,
) {

    internal fun håndter(hendelse: DayHasBegunEvent) {
        innsendingAdminService.hentInnsendingerSomErFerdigstilt()
            .forEach { journalpostId ->
                publiserInnsendingUtdatertHendelse(journalpostId)
            }
    }

    private fun publiserInnsendingUtdatertHendelse(journalpostId: String) {
        val id = UUID.randomUUID()

        mutableMapOf(
            "@event_name" to "InnsendingUtdatertHendelse",
            "@opprettet" to LocalDateTime.now(),
            "@id" to id,
            "journalpostId" to journalpostId,
        )
            .let { JsonMessage.newMessage(it) }
            .also { message ->
                SECURELOG.info { "Sender $id som ${message.toJson()}" }
                rapidsConnection.publish(journalpostId, message.toJson())
            }
    }
}
