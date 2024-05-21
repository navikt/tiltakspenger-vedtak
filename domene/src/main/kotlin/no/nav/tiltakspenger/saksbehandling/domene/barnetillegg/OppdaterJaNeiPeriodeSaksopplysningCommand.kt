package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.BarnetilleggBarnId
import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.Instant

data class OppdaterJaNeiPeriodeSaksopplysningCommand(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val detaljer: String,
    val tidspunkt: Instant,
    val bruker: Bruker,
    val barn: BarnetilleggBarnId,
    val verdi: Periodisering<JaNei?>,
)
