package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class TiltakDeltagelseSaksopplysningDTO(
    val tiltakNavn: String,
    val deltagelsePeriode: PeriodeDTO,
    val status: String,
    val kilde: TiltakDeltagelseKildeDTO,
)

internal fun TiltakDeltagelseSaksopplysning.toDTO(): TiltakDeltagelseSaksopplysningDTO {
    return TiltakDeltagelseSaksopplysningDTO(
        tiltakNavn = tiltakNavn,
        kilde = when (kilde) {
            "KOMET" -> TiltakDeltagelseKildeDTO.KOMET
            "ARENA" -> TiltakDeltagelseKildeDTO.ARENA
            else -> throw IllegalStateException("Ikke gyldig kilde for tiltaksdeltagelse")
        },
        deltagelsePeriode = deltagelsePeriode.toDTO(),
        status = status,
    )
}
