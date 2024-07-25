package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

data class LeggTilTiltakSaksopplysningCommand(
    val behandlingId: BehandlingId,
    val saksbehandler: Saksbehandler,
    val tiltak: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndring,
)
