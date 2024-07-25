package no.nav.tiltakspenger.vedtak.repository.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.TiltakSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class TiltakSaksopplysningDbJson(
    val tiltak: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): TiltakSaksopplysning {
        return when {
            saksbehandler != null -> TiltakSaksopplysning.Saksbehandler(
                tiltak = tiltak,
                årsakTilEndring = årsakTilEndring!!.toDomain(),
                saksbehandler = saksbehandler.toDomain(),
                tidsstempel = LocalDateTime.parse(tidsstempel),
            )

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for TiltakSaksopplysning.Personopplysning." }
                TiltakSaksopplysning.Søknad(
                    tiltak = tiltak,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }
    }

    enum class ÅrsakTilEndringDbJson {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
        ;

        fun toDomain(): ÅrsakTilEndring {
            return when (this) {
                FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
                ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
            }
        }
    }
}

internal fun TiltakSaksopplysning.toDbJson(): TiltakSaksopplysningDbJson {
    return TiltakSaksopplysningDbJson(
        tiltak = tiltak,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> TiltakSaksopplysningDbJson.ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> TiltakSaksopplysningDbJson.ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )
}
