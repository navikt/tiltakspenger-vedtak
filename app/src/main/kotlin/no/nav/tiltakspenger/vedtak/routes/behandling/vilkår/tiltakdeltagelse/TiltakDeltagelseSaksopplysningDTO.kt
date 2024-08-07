package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde.Arena
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde.Komet
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class TiltakDeltagelseSaksopplysningDTO(
    val tiltakNavn: String,
    val deltagelsePeriode: PeriodeDTO,
    val status: String,
    val kilde: TiltakDeltagelseKildeDTO,
)

internal fun TiltakDeltagelseSaksopplysning.toDTO(): TiltakDeltagelseSaksopplysningDTO =
    TiltakDeltagelseSaksopplysningDTO(
        tiltakNavn = tiltakNavn,
        kilde =
        when (kilde) {
            Arena -> TiltakDeltagelseKildeDTO.ARENA
            Komet -> TiltakDeltagelseKildeDTO.KOMET
        },
        deltagelsePeriode = deltagelsePeriode.toDTO(),
        status = status.toDTO(),
    )
