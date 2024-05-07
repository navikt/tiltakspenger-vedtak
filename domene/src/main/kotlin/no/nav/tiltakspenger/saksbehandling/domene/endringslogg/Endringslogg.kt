package no.nav.tiltakspenger.saksbehandling.domene.endringslogg

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.nå
import java.time.LocalDateTime

// TODO: Mutable eller ikke mutable?
data class Endringslogg private constructor(
    val kontekster: Map<String, String>,
    val endringsliste: List<Endring> = emptyList(),
) {
    companion object {
        operator fun invoke(sakId: SakId, behandlingId: BehandlingId): Endringslogg {
            return Endringslogg(kontekster(sakId, behandlingId))
        }

        operator fun invoke(sakId: SakId): Endringslogg {
            return Endringslogg(kontekster(sakId))
        }

        private fun kontekster(sakId: SakId, behandlingId: BehandlingId): Map<String, String> {
            return mapOf(
                "sakId" to sakId.toString(),
                "behandlingId" to behandlingId.toString(),
            )
        }

        private fun kontekster(sakId: SakId): Map<String, String> {
            return mapOf(
                "sakId" to sakId.toString(),
            )
        }
    }

    fun info(
        melding: String,
        bruker: Bruker?,
        tidsstempel: LocalDateTime = nå(),
    ): Endringslogg =
        this.copy(
            endringsliste = endringsliste +
                Endring.Info(
                    kontekster,
                    bruker?.brukernavn,
                    melding,
                    tidsstempel,
                ),
        )

    fun hendelse(
        type: Hendelsetype,
        melding: String,
        bruker: Bruker?,
        detaljer: Map<String, Any>,
        tidsstempel: LocalDateTime = nå(),
    ): Endringslogg =
        this.copy(
            endringsliste = endringsliste +
                Endring.Hendelse(
                    type,
                    kontekster,
                    bruker?.brukernavn,
                    melding,
                    detaljer,
                    tidsstempel,
                ),
        )
}
