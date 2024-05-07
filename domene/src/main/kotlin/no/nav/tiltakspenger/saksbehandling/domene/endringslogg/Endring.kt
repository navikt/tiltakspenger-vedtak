package no.nav.tiltakspenger.saksbehandling.domene.endringslogg

import no.nav.tiltakspenger.felles.nå
import java.time.LocalDateTime

sealed class Endring(
    val alvorlighetsgrad: Int,
    val label: Char,
    val brukernavn: String?,
    val melding: String,
    val tidsstempel: LocalDateTime,
    val kontekster: Map<String, String>,
) : Comparable<Endring> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Endring

        if (alvorlighetsgrad != other.alvorlighetsgrad) return false
        if (label != other.label) return false
        if (melding != other.melding) return false
        if (tidsstempel != other.tidsstempel) return false
        if (kontekster != other.kontekster) return false
        if (brukernavn != other.brukernavn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alvorlighetsgrad
        result = 31 * result + label.hashCode()
        result = 31 * result + melding.hashCode()
        result = 31 * result + tidsstempel.hashCode()
        result = 31 * result + kontekster.hashCode()
        result = 31 * result + brukernavn.hashCode()
        return result
    }

    override fun compareTo(other: Endring) =
        this.tidsstempel.compareTo(other.tidsstempel)
            .let { if (it == 0) other.alvorlighetsgrad.compareTo(this.alvorlighetsgrad) else it }

    class Hendelse(
        val type: Hendelsetype,
        kontekster: Map<String, String>,
        brukernavn: String?,
        melding: String,
        val detaljer: Map<String, Any> = emptyMap(),
        tidsstempel: LocalDateTime = nå(),
    ) : Endring(
        50,
        'H',
        brukernavn,
        melding,
        tidsstempel,
        kontekster,
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Hendelse

            if (type != other.type) return false
            if (detaljer != other.detaljer) return false
            if (brukernavn != other.brukernavn) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + detaljer.hashCode()
            return result
        }
    }

    class Info(
        kontekster: Map<String, String>,
        brukernavn: String?,
        melding: String,
        tidsstempel: LocalDateTime = nå(),
    ) : Endring(
        0,
        'I',
        brukernavn,
        melding,
        tidsstempel,
        kontekster,
    )
}
