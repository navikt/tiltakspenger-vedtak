package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles.ÅrsakTilEndringDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class TiltakSaksopplysningDTO(
    val tiltak: String,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val deltagelsePeriode: PeriodeDTO,
    val girRett: Boolean,
    val status: String,
    val kilde: TiltakKildeDTO,
)

internal fun TiltakDeltagelseSaksopplysning.toDTO(): TiltakSaksopplysningDTO {
    return TiltakSaksopplysningDTO(
        tiltak = tiltak,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = TiltakKildeDTO.valueOf(kilde),
        deltagelsePeriode = deltagelsePeriode.toDTO(),
        girRett = girRett,
        status = status,
    )
}
