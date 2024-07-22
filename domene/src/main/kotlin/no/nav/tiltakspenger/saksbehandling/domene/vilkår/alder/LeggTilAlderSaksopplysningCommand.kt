package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDate

data class LeggTilAlderSaksopplysningCommand(
    val behandlingId: BehandlingId,
    val saksbehandler: Saksbehandler,
    val fødselsdato: LocalDate,
    val årsakTilEndring: ÅrsakTilEndring,
)
