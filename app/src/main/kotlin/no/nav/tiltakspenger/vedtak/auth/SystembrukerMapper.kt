package no.nav.tiltakspenger.vedtak.auth

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.felles.Systembrukerrolle
import no.nav.tiltakspenger.felles.Systembrukerroller

private val logger = KotlinLogging.logger { }

fun systembrukerMapper(
    brukernavn: String,
    roller: Set<String>,
): Systembruker {
    return Systembruker(
        brukernavn = brukernavn,
        roller = Systembrukerroller(
            roller.mapNotNull { rolle ->
                when (rolle) {
                    "hente_data" -> Systembrukerrolle.HENTE_DATA
                    "lage_hendelser" -> Systembrukerrolle.LAGE_HENDELSER
                    else -> null.also {
                        logger.debug { "Filtrerer bort ukjent systembrukerrolle: $rolle" }
                    }
                }
            }.toSet(),
        ),
    )
}
