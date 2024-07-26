package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles.ÅrsakTilEndringDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class TiltakDeltagelseSaksopplysningDTO(
    val tiltakNavn: String,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val deltagelsePeriode: PeriodeDTO,
    val status: String,
    val kilde: TiltakDeltagelseKildeDTO,
)

internal fun TiltakDeltagelseSaksopplysning.toDTO(): TiltakDeltagelseSaksopplysningDTO {
    return TiltakDeltagelseSaksopplysningDTO(
        tiltakNavn = tiltakNavn,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = TiltakDeltagelseKildeDTO.valueOf(kilde),
        deltagelsePeriode = deltagelsePeriode.toDTO(),
        status = status,
    )
}
