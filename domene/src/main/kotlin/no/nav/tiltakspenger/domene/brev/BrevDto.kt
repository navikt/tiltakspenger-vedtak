package no.nav.tiltakspenger.domene.brev

import java.time.LocalDateTime

class BrevDTO(
    val personalia: Personalia,
    val tiltaksinfo: Tiltaksinfo,
    val fraDato: String,
    val tilDato: String,
    val saksnummer: String,
    val barnetillegg: Boolean,
    val saksbehandler: String,
    val kontor: String,
    val innsendingTidspunkt: LocalDateTime,
)

data class Personalia(
    val dato: String,
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val adresse: String,
    val husnummer: String,
    val bruksenhet: String,
    val postnummer: String,
    val poststed: String,
    val antallBarn: Int,
)

data class Tiltaksinfo(
    val tiltak: String,
    val tiltaksnavn: String,
    val tiltaksnummer: String,
    val arrangør: String,
)

enum class VedtaksTypeDTO(val navn: String, val skalSendeBrev: Boolean) {
    AVSLAG("Avslag", true),
    INNVILGELSE("Innvilgelse", true),
    STANS("Stans", true),
    FORLENGELSE("Forlengelse", true),
}
