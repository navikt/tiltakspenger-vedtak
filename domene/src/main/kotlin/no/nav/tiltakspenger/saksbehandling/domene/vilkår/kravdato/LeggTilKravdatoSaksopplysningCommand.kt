package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

data class LeggTilKravdatoSaksopplysningCommand(
    val behandlingId: BehandlingId,
    val saksbehandler: Saksbehandler,
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndring,
)
