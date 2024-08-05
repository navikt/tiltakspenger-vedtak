package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.LivsoppholdSaksopplysningDbJson.ÅrsakTilEndringLivsoppholdDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class LivsoppholdSaksopplysningDbJson(
    val harLivsoppholdYtelser: Boolean,
    val årsakTilEndring: ÅrsakTilEndringLivsoppholdDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
    val periode: PeriodeDbJson,
    val tidsstempel: String,
) {
    fun toDomain(): LivsoppholdSaksopplysning {
        return when {
            saksbehandler != null -> {
                LivsoppholdSaksopplysning.Saksbehandler(
                    harLivsoppholdYtelser = harLivsoppholdYtelser,
                    årsakTilEndring = årsakTilEndring?.toDomain(),
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                    saksbehandler = saksbehandler.toDomain(),
                    periode = periode.toDomain(),
                )
            }
            else -> {
                LivsoppholdSaksopplysning.Søknad(
                    harLivsoppholdYtelser = harLivsoppholdYtelser,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                    periode = periode.toDomain(),
                )
            }
        }
    }

    enum class ÅrsakTilEndringLivsoppholdDbJson {
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

internal fun LivsoppholdSaksopplysning.toDbJson(): LivsoppholdSaksopplysningDbJson {
    return LivsoppholdSaksopplysningDbJson(
        harLivsoppholdYtelser = harLivsoppholdYtelser,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> {
                ÅrsakTilEndringLivsoppholdDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
            }

            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> {
                ÅrsakTilEndringLivsoppholdDbJson.FEIL_I_INNHENTET_DATA
            }

            null -> null
        },
        saksbehandler = saksbehandler?.toDbJson(),
        periode = periode.toDbJson(),
        tidsstempel = tidsstempel.toString(),
    )
}
